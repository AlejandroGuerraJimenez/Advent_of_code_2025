# Reto Día 2 — Gift Shop

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 2 de Advent of Code 2025.

---

## 1. El problema

Una base de datos de la tienda de regalos contiene IDs de producto inválidos.
La entrada es una única línea con rangos de IDs separados por comas:

```
11-22,95-115,998-1012,...
```

Cada rango indica el primer y último ID a comprobar (separados por `-`).

### Parte 1 — Secuencia repetida exactamente 2 veces

Un ID es inválido si su representación como número tiene longitud par y la
primera mitad es igual a la segunda:

- `11` → `"1"` + `"1"` ✓
- `1010` → `"10"` + `"10"` ✓cre
- `123123` → `"123"` + `"123"` ✓

La respuesta es la **suma de todos los IDs inválidos** que aparecen en los rangos.

### Parte 2 — Secuencia repetida al menos 2 veces (`método 0x434C49434B`)

La regla se amplía: un ID es inválido si existe alguna subsecuencia de dígitos
que al repetirse 2 o más veces forma el número completo:

- `111` → `"1"` × 3 ✓ (nuevo en parte 2)
- `565656` → `"56"` × 3 ✓ (nuevo en parte 2)
- `824824824` → `"824"` × 3 ✓ (nuevo en parte 2)
- `2121212121` → `"21"` × 5 ✓ (nuevo en parte 2)

---

## 2. Estructura del paquete

```
dia2/
├── Day02.java              → implements Day; orquesta los dos retos
├── Parser.java             → parsea la línea de rangos en objetos de dominio
└── model/
    ├── IdRange.java        → record(long start, long end)
    └── InvalidIdChecker.java → reglas de negocio: detección y búsqueda
```

---

## 3. Flujo de ejecución

```
Day02.part1 / part2
  └─ Parser.parse(input)
        └─ input.strip().split(",")
             └─ Parser.parseRange(String) → IdRange(start, end)
  └─ sumInvalidIds(ranges, checker)
        └─ InvalidIdChecker.findInvalidIdsIn(range, checker)
              └─ itera id por id
                   └─ checker.test(id)
                        → isInvalid(id)         [parte 1]
                        → isInvalidExtended(id) [parte 2]
        └─ suma todos los IDs encontrados → String
```

---

## 4. Explicación clase por clase

### `Day02` (paquete `aoc.dia2`)

Implementa la interfaz `Day`. Su único trabajo es **orquestar**: parsear la
entrada, delegar la lógica de detección en `InvalidIdChecker` y devolver la
suma como `String`.

Contiene un método privado `sumInvalidIds(List<IdRange>, LongPredicate)` que
elimina la duplicación entre `part1` y `part2`: ambos retos hacen exactamente
lo mismo, solo cambia la regla de detección que se les pasa.

```java
// Parte 1: regla "exactamente 2 veces"
return sumInvalidIds(Parser.parse(input), InvalidIdChecker::isInvalid);

// Parte 2: regla "al menos 2 veces"
return sumInvalidIds(Parser.parse(input), InvalidIdChecker::isInvalidExtended);
```

### `Parser` (paquete `aoc.dia2`)

Convierte la única línea de entrada en una `List<IdRange>`:

1. `strip()` elimina espacios/saltos de línea sobrantes.
2. `split(",")` divide por rangos.
3. `parseRange(String)` extrae `start` y `end` de cada `"A-B"`.

Usa `long` para los extremos porque los IDs del input real superan
`Integer.MAX_VALUE` (~2.1 mil millones) y su suma lo hace con más margen.

### `IdRange` (paquete `aoc.dia2.model`)

`record` inmutable con los dos extremos del rango. Representa el dato de dominio
tal como lo describe el enunciado: un rango con inicio y fin. Al ser un `record`,
es inmutable por defecto y tiene `equals`, `hashCode` y `toString` gratuitos.

### `InvalidIdChecker` (paquete `aoc.dia2.model`)

Concentra **toda la lógica de detección** de IDs inválidos. Tiene tres métodos:

**`isInvalid(long id)`** — Regla de la parte 1.
Convierte el ID a `String`, comprueba longitud par y que la primera mitad sea
igual a la segunda:

```java
int half = s.length() / 2;
return s.substring(0, half).equals(s.substring(half));
```

**`isInvalidExtended(long id)`** — Regla de la parte 2.
Convierte el ID a `char[]` y prueba todos los posibles tamaños de patrón (1
hasta la mitad de la longitud). Para cada tamaño que divide exactamente la
longitud total, comprueba si todos los dígitos coinciden con su posición en el
patrón mediante `i % patternLen`. No crea objetos `String` intermedios:

```java
char[] digits = Long.toString(id).toCharArray();
for (int patternLen = 1; patternLen <= len / 2; patternLen++) {
    if (len % patternLen == 0 && isRepeatingPattern(digits, patternLen)) {
        return true;
    }
}
```

**`findInvalidIdsIn(IdRange, LongPredicate)`** — Itera el rango y aplica la
regla pasada como parámetro. Usa `LongPredicate` (interfaz funcional para
`long` primitivo) en vez de `Predicate<Long>` para evitar boxing/unboxing y los
problemas que genera en la JVM 25.

---

## 5. Principios de diseño aplicados

### SRP — Responsabilidad única

| Clase | Su único motivo de cambio |
|---|---|
| `Parser` | Cambia el formato de la línea de entrada |
| `IdRange` | Cambia la estructura de un rango |
| `InvalidIdChecker` | Cambia la definición de ID inválido |
| `Day02` | Cambia cómo se combinan las piezas para el reto |

### OCP — Abierto/Cerrado

Añadir el Día 2 solo requirió crear su paquete y registrar `Day02` en el
`Map` de `Main`. No se modificó `DayRunner`, `InputReader` ni `DayRegistry`.

### DRY — No te repitas

`part1` y `part2` comparten exactamente el mismo esqueleto
(parsear → buscar → sumar). En lugar de duplicarlo, se extrae a
`sumInvalidIds(List<IdRange>, LongPredicate)` que recibe la regla como
parámetro. Cambiar el algoritmo de suma solo requiere tocar un sitio.

```12:28:src/main/java/aoc/dia2/Day02.java
    @Override
    public String part1(String input) {
        return sumInvalidIds(Parser.parse(input), InvalidIdChecker::isInvalid);
    }

    @Override
    public String part2(String input) {
        return sumInvalidIds(Parser.parse(input), InvalidIdChecker::isInvalidExtended);
    }

    private String sumInvalidIds(List<IdRange> ranges, LongPredicate checker) {
        long sum = ranges.stream()
                .flatMap(range -> InvalidIdChecker.findInvalidIdsIn(range, checker).stream())
                .mapToLong(Long::longValue)
                .sum();
        return String.valueOf(sum);
    }
```

### Estrategia (Strategy Pattern)

`findInvalidIdsIn` acepta un `LongPredicate` como parámetro: la *estrategia*
de detección. `Day02` elige qué estrategia usar (`isInvalid` o
`isInvalidExtended`) sin que `findInvalidIdsIn` ni `IdRange` tengan que
conocer las distintas reglas. Añadir una tercera regla no implica modificar el
bucle de búsqueda.

### Value Object / Record

`IdRange` es un `record`: dato inmutable que modela exactamente un rango del
enunciado. Expresa la intención del dominio sin setters ni lógica.

```3:3:src/main/java/aoc/dia2/model/IdRange.java
public record IdRange(long start, long end) {}
```

### Separación parsing / cálculo

`Parser` transforma el texto en `List<IdRange>`. `InvalidIdChecker` trabaja con
`long` primitivos, sin conocer el formato de la entrada. Ambas capas pueden
probarse de forma completamente aislada.

---

## 6. Decisiones técnicas

### Por qué `long` y no `int`

El mayor ID del input real supera `Integer.MAX_VALUE` (2.147.483.647). La suma
de todos los IDs inválidos del input real es ~30.9 mil millones, bien fuera del
rango de `int`. Usar `long` desde el principio evita el problema.

### Por qué `LongPredicate` y no `Predicate<Long>`

`Predicate<Long>` trabaja con `Long` boxeado. En JVM 25, al pasar una referencia
a método `isInvalidExtended(long)` como `Predicate<Long>`, el compilador genera
un bridge lambda de unboxing que en ciertas condiciones produce un
`NullPointerException` por reutilización de slots de bytecode. `LongPredicate`
opera directamente con el primitivo `long`, sin boxing, y elimina el problema.

### Por qué comparación de caracteres y no `String.repeat` + `equals`

En `isInvalidExtended` la implementación original usaba:
```java
String pattern = s.substring(0, patternLen);
pattern.repeat(len / patternLen).equals(s)
```
Esto asigna un nuevo `String` por cada iteración del bucle. La implementación
final con `char[]` e `i % patternLen` no crea ningún objeto intermedio: es más
eficiente en memoria y evita el problema descrito con `Predicate<Long>`.

### Algoritmo de `isInvalidExtended`

Para un número de longitud `len`, se comprueban todos los divisores `patternLen`
de `len` en el rango `[1, len/2]`. Para cada uno se verifica que cada dígito
en la posición `i` coincida con el dígito `i % patternLen` del patrón. Si algún
divisor cumple la condición, el ID es inválido.

Complejidad: O(d(n) · len) donde d(n) es el número de divisores de `len`
(pequeño en la práctica, ya que los IDs tienen como máximo ~10 dígitos).

---

## 7. Resultados verificados

| Entrada | Parte 1 | Parte 2 |
|---|---|---|
| Ejemplo del enunciado | 1227775554 | 4174379265 |
| Input real | 24747430309 | 30962646823 |

---

## 8. Defensa del desarrollo

**¿Por qué `part1` y `part2` no tienen lógica propia?**
Porque ambos resuelven el mismo problema estructural: recorrer rangos, filtrar
IDs y sumar. La única diferencia es la regla de filtrado. Extraer esa diferencia
como parámetro (`LongPredicate`) elimina la duplicación y deja cada método con
una sola línea que describe exactamente qué hace: "suma los IDs inválidos según
esta regla".

**¿Por qué la regla de detección no está en `IdRange`?**
Porque `IdRange` es un dato (qué IDs existen), no un decisor (qué IDs son
inválidos). Mezclar ambas responsabilidades en `IdRange` obligaría a modificarlo
cada vez que cambie la definición de inválido. Separándolas, `IdRange` es
estable y `InvalidIdChecker` es el único punto de cambio.

**¿Qué pasaría si el enunciado añade una tercera regla de invalidez?**
Se añade un método estático en `InvalidIdChecker` y se pasa como `LongPredicate`
a `sumInvalidIds`. No se toca `Parser`, `IdRange`, ni el bucle de
`findInvalidIdsIn`. Eso es el principio abierto/cerrado en acción.

**¿Por qué no usas un enum o una clase Strategy explícita para las reglas?**
Con solo dos reglas, un `LongPredicate` pasado por referencia a método es
suficiente y más legible que una jerarquía de clases. Si el número de reglas
creciera (p. ej. 5 o más con lógica compleja), sería el momento de introducir
una interfaz `InvalidIdRule` con implementaciones concretas.

**¿Cómo testearías este código?**
- `IdRange`: trivial con un constructor y getters del record.
- `InvalidIdChecker.isInvalid` y `isInvalidExtended`: tests unitarios con los
  ejemplos del enunciado (11 → true, 99 → true solo en parte 2, 100 → false).
- `Parser.parse`: con la cadena de ejemplo, verificar que devuelve la lista de
  `IdRange` correcta.
- `Day02.part1` / `part2`: test de integración con el ejemplo completo del
  enunciado, esperando 1227775554 y 4174379265.
