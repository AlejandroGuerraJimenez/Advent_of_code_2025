# Reto Día 7 — Laboratories

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 7 de Advent of Code 2025.

---

## 1. El problema

Un manifold de taquiones es un grid 2D con un punto de entrada `S` y
divisores `^`. Los rayos de taquiones siempre se mueven **hacia abajo**.
Cuando un rayo alcanza un divisor `^`, se detiene y genera dos nuevos rayos
en la misma fila, en las columnas adyacentes izquierda y derecha.

### Parte 1 — ¿Cuántas veces se divide el haz?

Simular la propagación completa de los rayos con reglas de **fusión**: si dos
rayos convergen en la misma posición, se convierten en uno solo. Contar el
número de **divisiones únicas** (activaciones de splitters).

Ejemplo:
```
.......S.......
.......^.......   ← 1 división
......^.^......   ← 2 divisiones más (total 3)
.....^.^.^.....   ← etc.
...
```
→ **21 divisiones**

### Parte 2 — ¿Cuántas líneas temporales existen?

Aplicando la interpretación de los **muchos mundos**: cada vez que la partícula
llega a un divisor, la línea temporal se bifurca en dos (izquierda y derecha),
sin fusión. Contar el número total de líneas temporales al completar todos los
recorridos posibles.

Ejemplo: la misma malla → **40 líneas temporales**

---

## 2. Estructura de paquetes

```
aoc.dia7
├── Day07.java             ← Entry point del día
├── Parser.java            ← Conversión input → Manifold
└── model/
    ├── Position.java       ← Value object: (row, col) + hash key
    ├── Manifold.java       ← Grid inmutable con operaciones de consulta
    └── TachyonSimulator.java ← Lógica de simulación (BFS) y conteo de timelines
```

---

## 3. Flujo de ejecución

### Parte 1 — BFS con marcado de celdas
```
input (String)
  → Parser.parse()            filas del grid (SIN filtrar líneas vacías)
  → Manifold                  grid inmutable
  → TachyonSimulator.countSplits()
      BFS con visited set (celdas traversadas)
      al llegar a ^ no visitado: split++, encolar col±1
  → int
```

### Parte 2 — Recursión con memoización
```
input (String)
  → Parser.parse()
  → Manifold
  → TachyonSimulator.countTimelines()
      countFrom(row, col):
        si fuera del grid → 1
        si en memo        → memo[key]
        si no hay ^ abajo → 1
        si hay ^ en (r,c) → countFrom(r, c-1) + countFrom(r, c+1)
  → long
```

---

## 4. Explicación clase a clase

### `Position` — record
```java
public record Position(int row, int col) {
    public long key(int width) { return (long) row * width + col; }
}
```
Value object inmutable para representar coordenadas en el grid. El método
`key()` produce un identificador único `long` para usar en `HashSet`/`HashMap`
sin crear objetos extra. El factor `width` garantiza ausencia de colisiones
para posiciones válidas dentro del grid.

### `Manifold` — record
```java
public record Manifold(List<String> rows) {
    public boolean inBounds(Position p) { ... }
    public char at(Position p) { ... }  // devuelve '.' para filas cortas
    public Position start() { ... }     // localiza 'S'
}
```
Representa el grid de forma **inmutable**. El método `at()` es defensivo:
si una fila es más corta que el ancho máximo (por ejemplo, las filas vacías
en el grid tienen longitud 0), devuelve `'.'` en lugar de lanzar excepción.

**Decisión crítica**: el parser NO filtra líneas vacías. Las filas vacías son
parte del grid (espacio vacío por el que pasan los rayos). Filtrarlas
comprime el grid y hace que los rayos golpeen splitters demasiado pronto.

### `TachyonSimulator` — servicio de dominio (dos algoritmos)

#### Parte 1: BFS iterativo con fusión de rayos

El algoritmo es un **BFS** donde cada posición en la cola representa el punto
de inicio de un rayo. La clave del diseño es el conjunto `visited` que marca
**todas las celdas traversadas** (no solo los inicios de rayon).

```java
private static int processBeam(Manifold m, Queue<Position> q, Set<Long> visited, Position start) {
    if (!m.inBounds(start) || visited.contains(start.key(m.width()))) return 0;
    Position hit = scanDownMarking(m, visited, start);
    return m.inBounds(hit) && !visited.contains(hit.key(m.width())) ? split(m, q, visited, hit) : 0;
}
```

Hay **dos guardas** antes de contar un split:
1. `visited.contains(start.key(...))` — el inicio del rayo ya fue traversado
   por otro rayo anterior → fusión → skip.
2. `!visited.contains(hit.key(...))` — el splitter ya fue activado por otro
   rayo anterior → no contar de nuevo.

#### ¿Por qué dos guardas y no solo una?

La guarda 1 detecta el caso más común: dos rayos que empiezan en la misma
posición (mismo `startRow, startCol`) se fusionan al inicio. Pero existe un
caso más sutil: **el orden BFS no garantiza que el rayo de fila más alta
se procese antes**. Un rayo que empieza en fila 100 (pocas divisiones desde S)
puede procesarse antes que un rayo que empieza en fila 4 (más divisiones
desde S). Si el de fila 100 procesa primero, marca celdas desde la fila 100,
pero las celdas anteriores (filas 4-99) quedan sin marcar. El rayo de fila 4,
al procesar después, escanea hasta el mismo splitter y lo activaría de nuevo
sin la guarda 2. En el input real esto ocurría, elevando el conteo a **1711**
(superior al total de splitters, 1666), lo que delató el error.

```
S → nivel 1 → splitter fila 100 → rayo col 5, fila 100 (nivel 2)
           → nivel 3 → nivel 4 → rayo col 5, fila 4  (nivel 5)

Sin guarda 2: fila 100 procesa primero, marca 100-140.
              fila 4 procesa después, (4,5) no está en visited,
              escanea hasta splitter fila 140 → DOBLE ACTIVACIÓN
```

#### `scanDownMarking` — el corazón del BFS

```java
private static Position scanDownMarking(Manifold m, Set<Long> visited, Position start) {
    int r = start.row(), c = start.col();
    while (m.inBounds(new Position(r, c)) && m.at(new Position(r, c)) != '^')
        visited.add(new Position(r++, c).key(m.width()));
    return new Position(r, c);
}
```
Marca cada celda atravesada como visitada durante el escaneo (no solo al final).
Esto es lo que permite la fusión correcta: si dos rayos en la misma columna
se superponen en cualquier punto, el segundo es detectado y descartado.

#### Parte 2: Recursión con memoización descendente

Sin fusión, el conteo de líneas temporales sigue la recurrencia:

```
T(row, col) = 1                          si col fuera del grid (salida)
            = 1                          si no hay ^ en col debajo de row
            = T(r_s, col-1) + T(r_s, col+1)  si hay ^ en (r_s, col)
```

donde `r_s` es la fila del primer `^` en columna `col` a partir de `row`.

La memoización es **imprescindible**: sin ella la complejidad es exponencial
(el árbol de splitting tiene 70 niveles en el input real). Con memoización,
la misma subproblema `(row, col)` solo se computa una vez.

**Nota técnica**: se usa `containsKey`/`put` explícitos en lugar de
`computeIfAbsent`. Java lanza `ConcurrentModificationException` si se llama
a `computeIfAbsent` de forma reentrante sobre el mismo mapa (el lambda recurre
al mapa que está siendo modificado). El patrón explícito evita este bug del JDK.

```java
private static long countFrom(Manifold m, int row, int col, Map<Long, Long> memo) {
    if (!m.inBounds(new Position(row, col))) return 1L;
    long key = (long) row * m.width() + col;
    if (memo.containsKey(key)) return memo.get(key);
    return cache(key, compute(m, row, col, memo), memo);
}
```

### `Parser` — simple pero crítico
```java
public static Manifold parse(String input) {
    List<String> rows = input.lines().toList();  // SIN filtrar blancos
    return new Manifold(rows);
}
```
La decisión de **no filtrar líneas vacías** es la más importante del parser.
Las líneas vacías son filas de espacio vacío por las que los rayos pasan
libremente. Filtrarlas causó la primera respuesta incorrecta (2069 >> 21).

---

## 5. Principios de diseño aplicados

| Principio | Aplicación |
|-----------|-----------|
| **SRP** | `Manifold` solo gestiona el grid, `TachyonSimulator` solo simula, `Parser` solo parsea |
| **Inmutabilidad** | `Manifold` y `Position` son `record` — el grid nunca se modifica |
| **DRY** | `scanDownMarking` y `compute` comparten la misma lógica de "escanear hacia abajo"; `Manifold.at()` centraliza el acceso seguro al grid |
| **Encapsulación** | La complejidad del BFS con doble guarda está completamente oculta en métodos privados de `TachyonSimulator` |
| **Separación de algoritmos** | Parte 1 (BFS imperativo) y Parte 2 (recursión funcional) coexisten en la misma clase pero sin interferencia |

---

## 6. Decisiones técnicas

### ¿Por qué BFS en lugar de DFS para Parte 1?
BFS procesa los rayos en orden de "distancia desde S" (en número de divisiones).
Esto garantiza que, en la mayoría de casos, el rayo de fila más alta en una
columna dada se procese primero. La guarda 2 cubre los casos restantes donde
BFS no garantiza ese orden.

### ¿Por qué `key = row * width + col` y no un record como clave?
Usar un `long` como clave en `HashSet<Long>` evita el boxing/unboxing de
`HashSet<Position>` (que requeriría `equals` y `hashCode` en `Position`).
La fórmula `row * width + col` es una biyección para posiciones válidas
`0 ≤ col < width`, garantizando unicidad sin colisiones.

### ¿Por qué `width()` usa `max()` sobre todas las filas?
El input tiene filas vacías (longitud 0). Con `rows.get(0).length()` se usaría
la longitud de la primera fila, que puede diferir de la máxima si la primera
fila es la que contiene `S` y tiene longitud distinta. El `max()` garantiza que
`inBounds` y `key` sean correctos para todas las filas.

### ¿Por qué `at()` devuelve `'.'` para columnas fuera del largo de la fila?
Las filas vacías tienen `length() == 0`. Si `inBounds` considera que el punto
está en el grid (porque `col < width()`) pero la fila es más corta, `charAt`
lanzaría `StringIndexOutOfBoundsException`. El retorno defensivo `'.'` trata
las celdas faltantes como espacio vacío — lo que son semánticamente.

---

## 7. Resultados

| Parte | Respuesta |
|-------|-----------|
| Parte 1 | `1543` |
| Parte 2 | `3223365367809` |

---

## 8. Defensa del desarrollo

**P: ¿Por qué el BFS necesita dos guardas? ¿No basta con marcar el inicio?**

R: No. La guarda 1 (inicio visitado) cubre el caso donde dos rayos empiezan en
exactamente la misma celda. Pero un rayo de BFS-nivel bajo puede empezar en una
fila más profunda del grid que un rayo de BFS-nivel alto. Si el primero procesa
antes, no marca las celdas por encima de su inicio. El rayo posterior, al
empezar más arriba, llegaría al mismo splitter y lo contaría de nuevo. La guarda
2 (splitter ya visitado) es el seguro que impide ese doble conteo. En el input
real, sin la guarda 2 se obtenían 1711 activaciones, siendo el total de splitters
solo 1666 — la imposibilidad matemática delató el bug.

**P: ¿Por qué la Parte 2 no necesita gestionar fusión como la Parte 1?**

R: Precisamente porque en la interpretación de los muchos mundos NO HAY fusión.
Cada línea temporal es independiente. Si dos líneas temporales distintas tienen
una partícula en el mismo punto, siguen siendo dos líneas temporales distintas.
La memoización no implementa fusión; simplemente evita recalcular el número de
líneas temporales que salen de una posición ya conocida.

**P: ¿El resultado de Parte 2 (3.2 billones) no podría desbordar `long`?**

R: No. `long` en Java almacena valores hasta `9.2 × 10^18`. El resultado
`3.223.365.367.809 ≈ 3.2 × 10^12` está ampliamente dentro del rango.
Con 70 niveles de splitters y algunos miles de columnas, el árbol de splitting
podría teóricamente alcanzar `2^70 ≈ 10^21`, que sí desbordaría. Que el
resultado sea del orden de `10^12` se explica porque la mayoría de caminos
convergen hacia columnas sin splitters (que contribuyen 1 al conteo),
moderando el crecimiento exponencial.

**P: ¿Por qué `countTimelines` llama a `countFrom` con la fila de `S` y no
con la fila de abajo?**

R: Porque `countFrom(row, col)` escanea desde `row` inclusive. En la fila de S,
`at(row_S, col_S) == 'S'` que no es `'^'`, así que el escaneo avanza
correctamente a la siguiente fila. Empezar en `row_S + 1` también sería válido
pero menos general — si S estuviera en un borde o si se añadieran nuevas
variantes del puzzle.

---

## Mejoras arquitectónicas aplicadas

### Fase 1 — Core: interfaz `Day<T>` con parseo único
- `Day07` implementa ahora `Day<Manifold>`: el `Manifold` se **parsea una sola
  vez** y ambas partes operan sobre él (antes cada parte reparseaba).
- `part1`/`part2` devuelven `Object`; el `toString` lo hace `DayRunner`.
- Se añadió `number()`. La salida muestra la etiqueta y el resultado de cada parte.

### Fase 2 — Utilidades de parseo (`aoc.parse`)
- `Manifold` ahora **envuelve** `aoc.parse.TextGrid` (patrón Adapter): delega
  `width`/`height`/`at`/`inBounds` y conserva su API basada en `Position` y
  `start()`. Se elimina la reimplementación de la mecánica de rejilla (antes
  duplicada respecto al día 4).
- `Parser` = `new Manifold(TextGrid.fromLines(Lines.all(input)))`.
