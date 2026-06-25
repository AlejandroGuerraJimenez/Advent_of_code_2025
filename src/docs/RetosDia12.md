# Reto Día 12 — Christmas Tree Farm

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 12 de Advent of Code 2025.

---

## 1. El problema

La entrada tiene dos secciones:

1. **Figuras** de regalos (polióminos dentro de una caja 3×3), donde `#` es
   parte del regalo y `.` no.
2. **Regiones** bajo cada árbol: `12x5: 1 0 1 0 2 2` significa una rejilla de
   12 de ancho × 5 de alto, en la que hay que meter 1 regalo de la figura 0,
   1 de la figura 2, 2 de la figura 4 y 2 de la figura 5.

### Parte 1 — ¿Cuántas regiones admiten todos sus regalos?

Reglas de colocación:

- Los regalos se pueden **rotar y voltear**.
- Las `#` de dos regalos **no pueden solaparse**.
- Las `.` **no bloquean**: dos regalos pueden encajar uno en el hueco del otro.
- **Pueden quedar celdas vacías** → es un problema de **empaquetado (packing)**,
  no de cobertura exacta.

Hay que contar cuántas regiones permiten colocar todos los regalos pedidos.

Ejemplo:

```
4x4:  0 0 0 0 2 0   → caben          ✅
12x5: 1 0 1 0 2 2   → caben          ✅
12x5: 1 0 1 0 3 2   → NO caben       ❌  (¡aunque sobra área!)
```

Resultado del ejemplo → **2**.

> **Clave del problema**: la región 3 tiene área de sobra (49 celdas usadas de
> 60) y aun así no encaja. Por tanto, **el área no es condición suficiente**;
> hace falta un empaquetador real.

### Parte 2 — Estrella gratis

La segunda estrella se otorga automáticamente al completar el resto de días del
evento. No tiene puzzle propio.

---

## 2. Estructura de paquetes

```
aoc.dia12
├── Day12.java          ← Entry point del día
├── Parser.java         ← Fachada; delega en adapter/
├── adapter/            ← Adaptadores del formato de input (no confundir con aoc.parse)
│   ├── PuzzleAdapter.java
│   ├── ShapeParser.java
│   └── RegionParser.java
└── model/
    ├── PuzzleInput.java ← Modelo parseado (figuras + regiones)
    ├── Shape.java
    ├── Region.java
    └── Packer.java
```

---

## 3. Flujo de ejecución

```
input (String)
  → Parser.parse() / PuzzleAdapter.parse()   PuzzleInput(shapes[], List<Region>)
  → por cada Region:
       Packer.fits(region, shapes):
         requeridas = Σ counts[s] · shapes[s].cells()
         si requeridas > área        → NO         (cota necesaria, rigurosa)
         si no → search() empaqueta con backtracking
  → contar las que devuelven true
  → String (respuesta Parte 1)
```

---

## 4. Explicación clase a clase

### `Shape` — polimino y sus orientaciones

Cada figura se normaliza a una lista de coordenadas `{fila, col}` con la esquina
superior-izquierda en `(0,0)` y ordenada en **orden de escaneo** (fila, luego
columna). Se generan las hasta **8 orientaciones** (4 rotaciones × volteo) y se
descartan las duplicadas.

```java
public static Shape from(boolean[][] grid) {
    List<int[]> base = filledCells(grid);
    return new Shape(distinctOrientations(base), base.size());
}
```

La rotación 90° en sentido horario y el volteo horizontal se expresan como
transformaciones de coordenadas, centralizadas en un único helper `map` (DRY):

```java
private static List<int[]> rotate(List<int[]> coords) {
    return Arrays.asList(map(coords, cell -> new int[]{cell[1], -cell[0]}));
}

private static List<int[]> flip(List<int[]> coords) {
    return Arrays.asList(map(coords, cell -> new int[]{cell[0], -cell[1]}));
}
```

Que la primera coordenada de cada orientación sea la **primera `#` en orden de
escaneo** es la propiedad que aprovecha el empaquetador para anclar piezas.

---

### `Region` — value object

```java
public record Region(int width, int height, int[] counts) {
    public int area() { return width * height; }
}
```

---

### `Packer` — empaquetado con backtracking

#### Idea: DFS dirigido por la primera celda vacía

Se recorre la rejilla en orden de escaneo. En la **primera celda vacía** `c`,
en cualquier empaquetado válido esa celda solo puede:

1. estar **cubierta por un regalo**, o
2. quedar como **hueco permanente**.

Por eso el DFS prueba, en cada primera celda vacía:

```java
return tryPresents(pos, remaining, holeBudget)   // colocar un regalo en c
    || tryHole(pos, remaining, holeBudget);      // dejar c como hueco
```

#### Anclaje de la primera `#`

Al cubrir `c`, el regalo se coloca de modo que **su primera `#` (orden de
escaneo) caiga justo en `c`**. Como todas las demás celdas de la pieza vienen
después en orden de escaneo, ninguna toca celdas anteriores a `c` (ya decididas).
Esto garantiza dos cosas:

- **Completitud**: cada regalo se coloca exactamente cuando el escaneo llega a
  su primera `#`, que en ese momento es la primera celda vacía. Así el DFS
  explora todos los empaquetados posibles.
- **Eficiencia**: en cada celda solo hay que probar `nº figuras × orientaciones`
  colocaciones (≤ 6 × 8), más la opción de hueco.

```java
private int[][] absoluteCells(int[][] orientation, int pos) {
    int rOff = pos / w - orientation[0][0], cOff = pos % w - orientation[0][1];
    int[][] cells = new int[orientation.length][];
    for (int i = 0; i < orientation.length; i++)
        cells[i] = new int[]{orientation[i][0] + rOff, orientation[i][1] + cOff};
    return cells;
}
```

`absoluteCells` se comparte entre `placeable` (comprueba límites y solapes) y
`toggle` (marca/desmarca), eliminando la duplicación del cálculo del offset.

#### Poda

- **Cota de área** (necesaria y rigurosa): si `celdas_requeridas > área`, es
  imposible → se descarta antes de buscar.
- **Presupuesto de huecos** (`holeBudget = área − requeridas`): limita cuántas
  celdas pueden quedar vacías; al agotarse, fuerza colocaciones.

---

## 5. Análisis del input y validación

El input real tiene **1000 regiones**. Al analizarlo:

| Grupo | Nº regiones | Holgura (área − requeridas) |
|-------|-------------|------------------------------|
| Más celdas que área (imposibles) | 459 | −3 … −1 |
| Caben por área | 541 | **≥ 351** |

Hay un **salto enorme**: ninguna región tiene holgura pequeña positiva. Las
dimensiones mínimas son 35×35. Por eso, para *este* input, las 541 que caben por
área **caben de verdad** (el `Packer` construye un empaquetado para cada una en
la primera pasada, sin apenas backtracking).

Importante: el `Packer` **no asume** que el área baste. Lo demuestra el ejemplo,
donde la región 3 cabe por área (49 ≤ 60) pero el solver la rechaza
correctamente — por eso el ejemplo da **2** y no 3.

---

## 6. Decisiones de diseño

### Empaquetador real, no chequeo de área

El ejemplo prueba que el área es condición **necesaria pero no suficiente**. El
`Packer` resuelve el problema general; el chequeo de área es solo una poda
inicial rápida y rigurosa para el lado negativo.

### Orden de escaneo + anclaje

Procesar siempre la primera celda vacía y anclar la primera `#` allí convierte
un empaquetado con simetrías (muchas piezas idénticas, muchos órdenes de
colocación) en una búsqueda dirigida y sin colocaciones redundantes.

### DRY y métodos cortos

`Shape` centraliza las transformaciones de coordenadas en `map`; `Packer`
centraliza el cálculo de celdas absolutas en `absoluteCells`. La búsqueda se
descompone en métodos pequeños (`nextEmpty`, `tryPresents`, `tryShape`,
`attempt`, `tryHole`).

---

## 7. Resultados

| Parte | Respuesta | Algoritmo | Tiempo |
|-------|-----------|-----------|--------|
| 1 | **541** | Packing DFS + backtracking (poda de área) | ≈ 0,3 s |
| 2 | Estrella gratis | — | — |

---

## Mejoras arquitectónicas aplicadas

### Fase 1 — Core: interfaz `Day<T>` con parseo único
- `Day12` implementa ahora `Day<PuzzleInput>`: las figuras y regiones se
  **parsean una sola vez** y ambas partes operan sobre el modelo.
- `part1` cuenta las regiones que encajan; `part2` devuelve el mensaje de
  estrella gratis.
- `part1`/`part2` devuelven `Object`; el `toString` lo hace `DayRunner`.
- Se añadió `number()`. La salida muestra la etiqueta y el resultado de cada parte.
- La normalización `\r\n → \n` se trasladó a `InputReader` (antes estaba en el
  `Parser` del día).

### Enriquecimiento local — `adapter/` + `PuzzleInput`
- **`model/PuzzleInput`**: el record de entrada parseada deja de vivir en `Parser`.
- **`adapter/`** (no `parse/`): adaptadores del formato concreto del puzzle;
  evita confusión con el paquete transversal `aoc.parse`.
  - `ShapeParser` — bloques 3×3 → `Shape`
  - `RegionParser` — líneas `WxH: counts` → `Region`
  - `PuzzleAdapter` — orquesta el parseo completo
- `Parser.java` queda como fachada de una línea sobre `PuzzleAdapter`.
- Tests en `src/test/java/aoc/dia12/Day12Test.java` (JUnit 5).

#### Justificación

**Por qué enriquecer este día.** El parser mezclaba dos responsabilidades (figuras
3×3 y regiones `WxH: counts`) y el record `Input` vivía dentro de `Parser`, mezclando
formato de entrada con modelo de dominio. No era un monolito grande (~64 líneas),
pero la frontera entre capas no era honesta.

**Por qué `adapter/` y no `parse/`.** Ya existe el paquete transversal `aoc.parse`
(`Lines`, `LongRange`, `TextGrid`…): utilidades genéricas de troceo de texto. Una
carpeta `dia12.parse` usaría el mismo nombre para otra cosa (adaptar el formato
*concreto* del puzzle). `adapter/` sigue el patrón hexagonal: traduce input externo
→ tipos del dominio (`Shape`, `Region`, `PuzzleInput`), sin confundirse con la
infraestructura compartida.

**Por qué no `service/` ni `controller/`.** No hay HTTP, UI ni orquestación entre
agregados: `Day12` ya es el orquestador y `Packer` el algoritmo. Añadir capas
enterprise sería ceremonia sin una razón de cambio nueva.

**Por qué `PuzzleInput` en `model/`.** Es el resultado del parseo — el estado con el
que operan `part1` y `Packer` —, no la lógica de lectura. Pertenece al dominio del
día, no al adaptador.

**Por qué mantener `Parser.java` en la raíz.** Los otros 11 días exponen `Parser`
como punto de entrada del parseo; aquí delega en `adapter/` para no romper la
convención del proyecto.

**Tests.** Verifican que el adaptador produce un modelo válido y que la respuesta
del input real sigue siendo **541** tras el refactor.
