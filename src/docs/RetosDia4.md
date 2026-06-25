# Reto Día 4 — Printing Department

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 4 de Advent of Code 2025.

---

## 1. El problema

Los rollos de papel (`@`) están dispuestos en un grid 2D. Un roll es
**accesible** por una carretilla si tiene **menos de 4 vecinos `@`** en sus
8 posiciones adyacentes (horizontal, vertical y diagonal).

### Parte 1 — Cuántos rollos son accesibles

Dado el estado inicial del grid, contar cuántos `@` cumplen la condición de
accesibilidad.

Ejemplo: grid 10×10 → **13** rollos accesibles.

### Parte 2 — Cuántos rollos se pueden retirar en total

Simulación iterativa: en cada ronda se retiran **todos** los rollos accesibles
**simultáneamente**. Al retirar rollos, otros que antes no lo eran pueden pasar a
serlo. El proceso se repite hasta que ningún roll quede accesible. Contar el
total de rollos retirados.

Ejemplo: partiendo del mismo grid de 10×10 →
`13 + 12 + 7 + 5 + 2 + 1 + 1 + 1 + 1 = 43` rollos retirados.

---

## 2. Estructura del paquete

```
dia4/
├── Day04.java                  → implements Day; una línea por reto
├── Parser.java                 → texto → Grid
└── model/
    ├── Grid.java               → record inmutable con at(), inBounds()...
    └── ForkliftAccessChecker.java → lógica de accesibilidad y simulación
```

---

## 3. Flujo de ejecución

```
Day04.part1
  └─ Parser.parse(input) → Grid
  └─ ForkliftAccessChecker.countAccessible(grid)
        └─ por cada fila: countAccessibleInRow
              └─ por cada celda: isAccessible → adjacentRollCount

Day04.part2
  └─ Parser.parse(input) → Grid
  └─ ForkliftAccessChecker.countRemovable(grid)
        └─ mutableCopy(grid) → char[][]
        └─ do { removeRound(cells) } while (removed > 0)
              └─ collectAccessible → isRollAccessible → neighborRolls → isRollAt
              └─ eliminar posiciones encontradas → cells[r][c] = '.'
```

---

## 4. Explicación clase por clase

### `Day04` (paquete `aoc.dia4`)

Implementa `Day`. Cada reto es una sola línea: parsea e invoca al checker.
No contiene lógica de dominio.

```8:16:src/main/java/aoc/dia4/Day04.java
    @Override
    public String part1(String input) {
        return String.valueOf(ForkliftAccessChecker.countAccessible(Parser.parse(input)));
    }

    @Override
    public String part2(String input) {
        return String.valueOf(ForkliftAccessChecker.countRemovable(Parser.parse(input)));
    }
```

### `Parser` (paquete `aoc.dia4`)

Convierte el texto en un `Grid`: divide por líneas, filtra las vacías y las
empaqueta en la lista de filas del record.

### `Grid` (paquete `aoc.dia4.model`)

`record` inmutable con `List<String> rows`. Expone métodos de consulta:

- `height()` / `width()` → dimensiones del grid
- `at(row, col)` → carácter en esa posición
- `inBounds(row, col)` → si la posición está dentro del grid

No expone mutación: para la simulación se crea una copia `char[][]` separada.

```5:13:src/main/java/aoc/dia4/model/Grid.java
public record Grid(List<String> rows) {

    public int height() { return rows.size(); }
    public int width()  { return rows.isEmpty() ? 0 : rows.get(0).length(); }
    public char at(int row, int col) { return rows.get(row).charAt(col); }
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < height() && col >= 0 && col < width();
    }
}
```

### `ForkliftAccessChecker` (paquete `aoc.dia4.model`)

Toda la lógica de accesibilidad y simulación. Tiene dos grupos de métodos:

**Parte 1 — sobre `Grid` inmutable:**

- `countAccessible(Grid)` → suma por filas
- `countAccessibleInRow(Grid, row)` → filtra celdas accesibles en la fila
- `isAccessible(Grid, row, col)` → es `@` y tiene < 4 vecinos `@`
- `adjacentRollCount(Grid, row, col)` → cuenta los 8 vecinos que son `@`

**Parte 2 — sobre `char[][]` mutable:**

- `countRemovable(Grid)` → convierte a mutable y repite rondas hasta fin
- `mutableCopy(Grid)` → `List<String>` → `char[][]`
- `removeRound(char[][])` → colecta accesibles, los elimina, devuelve cuántos
- `collectAccessible(char[][])` → lista de posiciones [r,c] accesibles
- `isRollAccessible(char[][], r, c)` → es `@` y tiene < 4 vecinos `@`
- `neighborRolls(char[][], r, c)` → cuenta vecinos `@`
- `isRollAt(char[][], r, c)` → bounds check + comprobación de `@`

---

## 5. Principios de diseño aplicados

### SRP — Responsabilidad única

| Clase | Su único motivo de cambio |
|---|---|
| `Parser` | Cambia el formato del grid en el input |
| `Grid` | Cambia la estructura de datos del grid |
| `ForkliftAccessChecker` | Cambia la definición de accesibilidad o la simulación |
| `Day04` | Cambia cómo se orquesta la solución |

### Separación inmutable / mutable

`Grid` es un `record` inmutable: correcto para leer el estado inicial y para la
parte 1 (snapshot puntual). Para la parte 2 se necesita mutar el estado ronda a
ronda; se usa `char[][]` como representación interna de la simulación.

Esta separación tiene dos ventajas:
1. `Grid` no expone `remove()` ni ninguna operación destructiva, no puede
   corromperse accidentalmente.
2. `char[][]` es eficiente para actualizaciones en sitio sin crear objetos nuevos
   en cada ronda.

### Simultaneidad en la simulación

El orden de las operaciones en `removeRound` es deliberado:

```55:59:src/main/java/aoc/dia4/model/ForkliftAccessChecker.java
    private static long removeRound(char[][] cells) {
        List<int[]> targets = collectAccessible(cells);
        targets.forEach(p -> cells[p[0]][p[1]] = '.');
        return targets.size();
    }
```

Primero se **colectan** todas las posiciones accesibles con el estado actual, y
**después** se eliminan todas. Esto garantiza que la eliminación de un roll en
esta ronda no afecta a la accesibilidad de sus vecinos dentro de la misma ronda:
la simulación es simultánea, no secuencial.

Si se eliminara cada roll al encontrarlo, un roll recién eliminado reduciría el
conteo de vecinos de los adyacentes en la misma pasada, pudiendo marcarlos como
accesibles prematuramente.

### `DIRS` como constante expresiva

Las 8 direcciones adyacentes están declaradas como una constante `int[][]` con
formato visual que refleja la disposición real en el grid:

```10:15:src/main/java/aoc/dia4/model/ForkliftAccessChecker.java
    private static final int[][] DIRS = {
        {-1,-1}, {-1,0}, {-1,1},
        { 0,-1},         { 0,1},
        { 1,-1}, { 1,0}, { 1,1}
    };
```

El hueco central hace evidente que no se incluye `{0,0}` (la propia celda).

### OCP — Abierto/Cerrado

Añadir los días anteriores o posteriores no modifica nada del Día 4. El
`ForkliftAccessChecker` es independiente de la infraestructura; si cambiara
la regla de accesibilidad (p. ej. "menos de 3 vecinos"), solo cambia
`MAX_ADJACENT`.

---

## 6. Decisiones técnicas

### Por qué dos representaciones del grid

| Representación | Uso | Motivo |
|---|---|---|
| `Grid (record)` | Parte 1 | Snapshot inmutable, solo lectura |
| `char[][]` | Parte 2 | Mutación eficiente en sitio, sin objetos intermedios |

Crear un nuevo `Grid` en cada ronda de la simulación generaría `O(rondas × filas)`
objetos `String`. Con `char[][]` cada celda se actualiza directamente con un
`cells[r][c] = '.'` sin ninguna alocación.

### Por qué `long` en el retorno

El grid del input real tiene 136 filas. Aunque el número de rollos por sí mismo
cabría en `int`, usar `long` es consistente con el resto del proyecto y evita
sorpresas si el input es más grande.

### Bounds checking unificado en `isRollAt`

Para el `char[][]` el bounds check y la comprobación de `@` se fusionan en un
solo método:

```79:81:src/main/java/aoc/dia4/model/ForkliftAccessChecker.java
    private static boolean isRollAt(char[][] cells, int r, int c) {
        return r >= 0 && r < cells.length && c >= 0 && c < cells[r].length && cells[r][c] == '@';
    }
```

Esto simplifica `neighborRolls` a un único `filter` sin necesitar dos pasos
separados (bounds + valor), como sí hace la versión de `Grid` (que tiene
`inBounds` y `at` por separado).

---

## 7. Resultados verificados

| Entrada | Parte 1 | Parte 2 |
|---|---|---|
| Ejemplo del enunciado (10×10) | 13 | 43 |
| Input real (136 filas) | 1489 | 8890 |

---

## 8. Defensa del desarrollo

**¿Por qué la simulación elimina todos los accesibles a la vez y no de uno en uno?**
El enunciado dice que los forklifts trabajan en paralelo: "once a roll can be
accessed, it can be removed". Si eliminásemos secuencialmente, el orden de
eliminación cambiaría el resultado porque un roll recién eliminado alteraría la
accesibilidad de sus vecinos en la misma ronda. La eliminación simultánea es la
interpretación correcta y la que el enunciado valida con su ejemplo (13 en la
primera ronda, no más).

**¿Por qué `Grid` es inmutable si luego necesitas mutarla?**
Porque `Grid` modela el estado de entrada, que nunca debería modificarse. El
estado mutable de la simulación es una copia interna de `ForkliftAccessChecker`
que el exterior nunca ve. Esto sigue el principio de que los datos del dominio
son inmutables y la lógica de simulación maneja su propia representación interna.

**¿Por qué existe `Grid` en lugar de usar `char[][]` desde el principio?**
`Grid` con `List<String>` es la representación natural del input (que llega como
texto). Tiene métodos expresivos (`at`, `inBounds`, `height`, `width`) que hacen
legible la parte 1. Si se usara `char[][]` desde el Parser, se mezclaría la
lectura de datos con la estructura de simulación.

**¿Cómo escalaría si el grid fuera de un millón de filas?**
La parte 1 es O(n×m) con un solo recorrido: escala bien. La parte 2 es
O(rondas × n×m); el número de rondas está acotado por la altura del grid en el
peor caso. Para grids enormes se podría optimizar usando una cola de posiciones
candidatas en lugar de reescanear todo el grid en cada ronda.

**¿Qué testearías primero?**
`isRollAt` (bounds + valor), `adjacentRollCount` con casos límite (esquina,
borde, centro rodeado), y `removeRound` con un mini-grid donde el resultado es
conocido. Todo son funciones puras o con estado predecible.

---

## Mejoras arquitectónicas aplicadas

### Fase 1 — Core: interfaz `Day<T>` con parseo único
- `Day04` se **parsea una sola vez** y ambas partes operan sobre la rejilla.
- `part1`/`part2` devuelven `Object`; el `toString` lo hace `DayRunner`.
- Se añadió `number()`. La salida muestra la etiqueta y el resultado de cada parte.

### Fase 2 — Utilidades de parseo (`aoc.parse`)
- Se eliminó el record local `Grid`: el día usa ahora `aoc.parse.TextGrid`
  (rejilla compartida con el día 7). `Day04` es `Day<TextGrid>` y
  `ForkliftAccessChecker` opera sobre `TextGrid`.
- `Parser` = `TextGrid.fromLines(Lines.nonBlank(input))`.
