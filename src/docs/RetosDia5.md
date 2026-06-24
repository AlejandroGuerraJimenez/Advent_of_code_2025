# Reto Día 5 — Cafeteria

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 5 de Advent of Code 2025.

---

## 1. El problema

La base de datos de ingredientes tiene dos secciones separadas por una línea en
blanco:

```
3-5          ← rangos de IDs frescos (sección 1)
10-14
16-20
12-18
             ← línea en blanco
1            ← IDs disponibles a comprobar (sección 2)
5
8
11
17
32
```

Un ID es **fresco** si cae dentro de al menos uno de los rangos (los rangos son
inclusivos y pueden solaparse).

### Parte 1 — ¿Cuántos IDs disponibles son frescos?

De la lista de IDs disponibles (sección 2), contar cuántos caen en algún rango.

Ejemplo: IDs `5`, `11`, `17` → **3** frescos.

### Parte 2 — ¿Cuántos IDs en total cubren los rangos?

Ignorar la sección 2. Contar todos los IDs únicos cubiertos por al menos un
rango de la sección 1.

Ejemplo: rangos `3-5, 10-14, 16-20, 12-18` → fusionados: `3-5, 10-20` →
3 + 11 = **14** IDs frescos en total.

---

## 2. Estructura del paquete

```
dia5/
├── Day05.java                  → implements Day; una línea por reto
├── Parser.java                 → divide el input en dos secciones → IngredientDatabase
└── model/
    ├── FreshRange.java         → record(long start, long end) + contains(id)
    ├── IngredientDatabase.java → record(List<FreshRange>, List<Long>)
    └── FreshnessChecker.java   → countFresh (p1) + countAllFresh (p2)
```

---

## 3. Flujo de ejecución

```
Day05.part1
  └─ Parser.parse(input) → IngredientDatabase
        └─ lines.indexOf("") → blankIdx
        └─ parseRanges → List<FreshRange>
        └─ parseIds    → List<Long>
  └─ FreshnessChecker.countFresh(db)
        └─ por cada ID: isFresh → FreshRange.contains(id)

Day05.part2
  └─ Parser.parse(input).freshRanges() → List<FreshRange>
  └─ FreshnessChecker.countAllFresh(ranges)
        └─ mergeRanges → ordenar + fusionar solapados
        └─ suma (end - start + 1) de cada rango fusionado
```

---

## 4. Explicación clase por clase

### `Day05` (paquete `aoc.dia5`)

Implementa `Day`. Cada reto es una sola línea sin lógica propia:

```8:16:src/main/java/aoc/dia5/Day05.java
    @Override
    public String part1(String input) {
        return String.valueOf(FreshnessChecker.countFresh(Parser.parse(input)));
    }

    @Override
    public String part2(String input) {
        return String.valueOf(FreshnessChecker.countAllFresh(Parser.parse(input).freshRanges()));
    }
```

### `Parser` (paquete `aoc.dia5`)

Localiza la línea en blanco con `lines.indexOf("")` para dividir el input sin
depender de expresiones regulares ni de separadores de línea del sistema
operativo. Extrae cada sección con `subList` y las convierte en objetos de
dominio.

```10:13:src/main/java/aoc/dia5/Parser.java
    public static IngredientDatabase parse(String input) {
        List<String> lines = input.lines().toList();
        int blankIdx = lines.indexOf("");
        return new IngredientDatabase(parseRanges(lines, blankIdx), parseIds(lines, blankIdx));
    }
```

### `FreshRange` (paquete `aoc.dia5.model`)

`record` inmutable con `start` y `end`. Sabe si contiene un ID mediante
`contains(long id)`: el rango es dueño de su propia regla de pertenencia, de
modo que el checker no necesita tocar `start`/`end` directamente (modelo de
dominio rico).

```3:8:src/main/java/aoc/dia5/model/FreshRange.java
public record FreshRange(long start, long end) {

    public boolean contains(long id) {
        return id >= start && id <= end;
    }
}
```

### `IngredientDatabase` (paquete `aoc.dia5.model`)

`record` inmutable que agrupa las dos partes del input: los rangos frescos y los
IDs disponibles. Actúa como objeto de transferencia del resultado del parser al
checker. Al ser un record, ambos accesores (`freshRanges()`, `ingredientIds()`)
son generados automáticamente.

### `FreshnessChecker` (paquete `aoc.dia5.model`)

Toda la lógica de frescura. Cuatro métodos con responsabilidades distintas:

**`countFresh(IngredientDatabase)`** — Parte 1. Filtra los IDs disponibles con
`isFresh` y los cuenta.

**`isFresh(long, List<FreshRange>)`** — Comprueba si algún rango contiene el
ID, delegando en `FreshRange.contains`.

**`countAllFresh(List<FreshRange>)`** — Parte 2. Fusiona los rangos y suma sus
tamaños.

**`mergeRanges(List<FreshRange>)`** — Ordena por `start` y aplica `mergeInto`
a cada rango.

**`mergeInto(List<FreshRange>, FreshRange)`** — Si el nuevo rango se solapa o es
adyacente al último fusionado, lo extiende; si no, lo añade como nuevo.

---

## 5. El algoritmo de fusión de rangos (parte 2)

La parte 2 podría resolverse iterando cada ID desde el mínimo al máximo, pero
el resultado del input real es ~352 billones de IDs frescos: iterar uno a uno
sería completamente inviable.

La solución correcta es fusionar rangos solapados y sumar sus longitudes:

**Paso 1 — Ordenar por `start`:**
```
3-5, 10-14, 16-20, 12-18  →  3-5, 10-14, 12-18, 16-20
```

**Paso 2 — Fusionar:**
- `3-5` → resultado: `[3-5]`
- `10-14`: `10 > 5+1` → no solapa → `[3-5, 10-14]`
- `12-18`: `12 <= 14+1=15` → solapa → extender: `[3-5, 10-18]`
- `16-20`: `16 <= 18+1=19` → solapa → extender: `[3-5, 10-20]`

**Paso 3 — Sumar tamaños:**
`(5-3+1) + (20-10+1) = 3 + 11 = 14` ✓

La condición de fusión es `next.start() <= last.end() + 1`: el `+1` cubre
rangos adyacentes sin hueco (p. ej. `3-5` y `6-8` forman `3-8`).

```35:41:src/main/java/aoc/dia5/model/FreshnessChecker.java
    private static void mergeInto(List<FreshRange> merged, FreshRange next) {
        FreshRange last = merged.get(merged.size() - 1);
        if (next.start() <= last.end() + 1)
            merged.set(merged.size() - 1, new FreshRange(last.start(), Math.max(last.end(), next.end())));
        else
            merged.add(next);
    }
```

El `Math.max(last.end(), next.end())` es necesario porque un rango puede estar
completamente contenido en otro (p. ej. `10-20` seguido de `12-15`): en ese
caso `next.end() < last.end()` y no hay que recortar el extremo derecho.

---

## 6. Principios de diseño aplicados

### SRP — Responsabilidad única

| Clase | Su único motivo de cambio |
|---|---|
| `Parser` | Cambia el formato del input (separador, estructura) |
| `FreshRange` | Cambia la definición de un rango o su regla de pertenencia |
| `IngredientDatabase` | Cambia la estructura del modelo de entrada |
| `FreshnessChecker` | Cambia la lógica de frescura o el algoritmo de fusión |
| `Day05` | Cambia cómo se orquesta la solución |

### Modelo de dominio rico en `FreshRange`

`FreshRange` no es solo datos: sabe si contiene un ID. Esto evita que
`FreshnessChecker` acceda directamente a `start` y `end`, respetando la
encapsulación y centralizando la regla de pertenencia en el tipo que la posee.

### Diferencia de complejidad entre parte 1 y parte 2

| Parte | Algoritmo | Complejidad |
|---|---|---|
| 1 | Por cada ID disponible, busca en todos los rangos | O(IDs × rangos) |
| 2 | Fusionar rangos + sumar tamaños | O(rangos × log rangos) |

La parte 1 es viable porque la lista de IDs disponibles es pequeña (ej. 6 en el
ejemplo). La parte 2 necesita la fusión porque el espacio de IDs cubiertos es de
cientos de billones.

### OCP — Abierto/Cerrado

`FreshnessChecker.countAllFresh` y `countFresh` son independientes: añadir una
parte 3 (p. ej. "cuántos rangos distintos son necesarios") no modifica los
métodos existentes.

---

## 7. Resultados verificados

| Entrada | Parte 1 | Parte 2 |
|---|---|---|
| Ejemplo del enunciado | 3 | 14 |
| Input real | 674 | 352509891817881 |

---

## 8. Defensa del desarrollo

**¿Por qué el algoritmo de fusión para la parte 2 y no iterar cada ID?**
El resultado de la parte 2 con el input real es ~352 billones. Iterar cada ID
a 10⁹ operaciones/segundo tardaría ~4 días. La fusión de rangos es O(n log n)
y termina en milisegundos.

**¿Por qué `contains` vive en `FreshRange` y no en `FreshnessChecker`?**
Porque `FreshRange` es el dueño natural de la regla "¿está este ID dentro de
mí?". Si la condición cambiara (p. ej. rangos exclusivos), solo cambiaría
`FreshRange.contains`, no el checker. Es el principio de que el conocimiento
vive en el tipo que posee los datos.

**¿Por qué `IngredientDatabase` existe como record y no se pasan dos listas por separado?**
Porque el input es conceptualmente una base de datos con dos partes. Si la parte
2 ignorase los IDs disponibles, el parser seguiría devolviendo
`IngredientDatabase` con los dos campos; el caller decide qué usar (`.freshRanges()`
o el objeto completo). La base de datos no cambia aunque cambie quién la consulta.

**¿Qué pasa si no hay línea en blanco en el input?**
`lines.indexOf("")` devuelve `-1`, y `subList(0, -1)` lanzaría
`IllegalArgumentException`. El enunciado garantiza la línea en blanco, así que
no se valida. En producción añadiría una precondición o excepción descriptiva.

**¿Cómo testearías `mergeRanges`?**
Con cuatro casos: rangos sin solapamiento, con solapamiento parcial, un rango
completamente contenido en otro, y rangos adyacentes sin hueco. Son casos
límite del `+1` en la condición de fusión.
