# Reto Día 9 — Movie Theater

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 9 de Advent of Code 2025.

---

## 1. El problema

El suelo del teatro está decorado con teselas. Algunas son rojas. Las teselas
rojas están conectadas entre sí (en orden de lista, incluyendo la última con la
primera) por segmentos rectos de teselas verdes. Además, todas las teselas
estrictamente dentro del polígono que forman son también verdes.

Se quiere encontrar el **rectángulo de mayor área** cuyos dos esquinas opuestas
sean teselas rojas. El área de un rectángulo con esquinas en `(x1,y1)` y
`(x2,y2)` es `(|x2-x1|+1) × (|y2-y1|+1)` porque las teselas tienen tamaño 1×1
y los extremos se incluyen.

### Parte 1 — Mayor rectángulo sin restricciones

Cualquier par de teselas rojas puede ser las esquinas opuestas. Simplemente
encontrar el par que maximiza el área.

Ejemplo (8 teselas rojas) → **50** (entre `2,5` y `11,1`)

### Parte 2 — Mayor rectángulo dentro del polígono rojo-verde

Ahora, **todas** las teselas del rectángulo (no solo las esquinas) deben ser
rojas o verdes. Esto restringe enormemente las opciones.

Ejemplo → **24** (entre `9,5` y `2,3`)

---

## 2. Estructura de paquetes

```
aoc.dia9
├── Day09.java               ← Entry point del día
├── Parser.java              ← Convierte líneas "X,Y" en Tile
└── model/
    ├── Tile.java             record(int x, int y)
    ├── RectangleSolver.java  maxArea() y maxValidArea()
    └── CompressedPolygon.java  grid comprimido + flood fill + prefix sum
```

---

## 3. Flujo de ejecución

### Parte 1 — O(N²)
```
input (String)
  → Parser.parse()                  lista de Tile
  → RectangleSolver.maxArea()
      for i in 0..N: for j in i+1..N:
        area = (|xi-xj|+1) * (|yi-yj|+1)
        max = Math.max(max, area)
  → long
```

### Parte 2 — O(N²) + O(K²) preproceso
```
input (String)
  → Parser.parse()
  → RectangleSolver.maxValidArea()
      CompressedPolygon(tiles):
        1. unique xs[], ys[]                  ≤N valores distintos cada uno
        2. buildBoundary()                    marcar segmentos en grid comprimido
        3. floodExterior()                    BFS desde el borde del grid
        4. buildPrefix()                      prefix sum 2D sobre celdas válidas
      for i,j: if poly.isValid(ti, tj) → max area
  → long
```

---

## 4. Explicación clase a clase

### `Tile` — record
```java
public record Tile(int x, int y) {}
```
Value object mínimo. Las coordenadas son `int` porque las posiciones del input
caben en 32 bits. Sin embargo, el **área** se calcula en `long` para evitar
overflow con coordenadas del orden de 10^5 (máx. área posible: ~10^10).

### `RectangleSolver` — servicio de dominio

**Parte 1 — búsqueda exhaustiva:**
```java
public static long maxArea(List<Tile> tiles) {
    long max = 0;
    for (int i = 0; i < tiles.size(); i++)
        for (int j = i + 1; j < tiles.size(); j++)
            max = Math.max(max, area(tiles.get(i), tiles.get(j)));
    return max;
}
```
Con N=496, hay 496×495/2 ≈ 123k pares. El doble bucle tarda milisegundos.
No hay estructura más eficiente que explotar aquí porque los ejes X e Y de
los puntos están acoplados (no podemos tomar el `max_x` de un punto y el
`max_y` de otro arbitrariamente).

**Parte 2 — validación por rectángulo:**
```java
public static long maxValidArea(List<Tile> tiles) {
    CompressedPolygon poly = new CompressedPolygon(tiles);
    long max = 0;
    for (int i = 0; i < tiles.size(); i++)
        for (int j = i + 1; j < tiles.size(); j++)
            if (poly.isValid(tiles.get(i), tiles.get(j))) max = Math.max(max, area(tiles.get(i), tiles.get(j)));
    return max;
}
```
Mismo bucle O(N²), pero cada consulta de validez es O(1) gracias al prefix sum.

**Fórmula de área con extremos inclusivos:**
```java
private static long area(Tile a, Tile b) {
    return (long) (Math.abs(a.x() - b.x()) + 1) * (Math.abs(a.y() - b.y()) + 1);
}
```
El `+1` en cada dimensión refleja que una tesela ocupa una unidad de espacio.
Sin él, la distancia entre `(7,1)` y `(11,7)` daría `4×6=24` en lugar de
`5×7=35`. El cast a `long` es crítico: `(int)(10^5+1) * (10^5+1)` desborda
`int` (máx. 2.1×10^9), pero 10^10 cabe en `long`.

### `CompressedPolygon` — el corazón del Día 9

El grid real puede ser hasta 10^5 × 10^5 = 10^10 celdas. Procesarlo
directamente es imposible. La solución es la **compresión de coordenadas**.

#### Idea fundamental: el grid comprimido (2N-1) × (2N-1)

Con N teselas rojas hay a lo sumo N valores únicos de X y N de Y. El grid
comprimido tiene `(2N-1) × (2N-1)` celdas (≈991×991 para N=496):

```
Columna 2i   → exactamente x = xs[i]
Columna 2i+1 → cualquier x en (xs[i], xs[i+1])   [el "hueco" entre dos X]
Fila    2j   → exactamente y = ys[j]
Fila    2j+1 → cualquier y en (ys[j], ys[j+1])
```

Entre dos valores consecutivos del polígono, el estado interior/exterior no
cambia, así que todas las celdas del hueco tienen el mismo estado. Esto es
correcto porque los segmentos del polígono solo cambian el estado en los
valores exactos de los vértices.

#### Paso 1: `buildBoundary` — marcar la frontera

```java
private void markSegment(boolean[][] b, Tile from, Tile to) {
    int c1 = cx(Math.min(from.x(), to.x())), c2 = cx(Math.max(from.x(), to.x()));
    int r1 = cy(Math.min(from.y(), to.y())), r2 = cy(Math.max(from.y(), to.y()));
    for (int c = c1; c <= c2; c++) for (int r = r1; r <= r2; r++) b[c][r] = true;
}
```

Para un segmento horizontal de `(xa, ya)` a `(xb, ya)`:
- `c1 = cx(xa) = 2*idx(xa)`, `c2 = cx(xb) = 2*idx(xb)` (ambos pares)
- `r1 = r2 = cy(ya)` (fila par, valor exacto)
- Marca toda la franja de columnas entre xa y xb, incluyendo los huecos impares

Los huecos entre valores consecutivos de X quedan marcados como frontera
porque el segmento físicamente pasa por ellos. Para un segmento vertical,
análogo con filas.

#### Paso 2: `floodExterior` — BFS desde el exterior

```java
private boolean[][] floodExterior(boolean[][] b) {
    boolean[][] ext = new boolean[b.length][b[0].length];
    Queue<int[]> q = new LinkedList<>();
    seedBorder(q, ext, b);
    while (!q.isEmpty()) spread(q, ext, b, b.length, b[0].length);
    return ext;
}
```

BFS clásico 4-conexo empezando desde todas las celdas del borde del grid
comprimido que no sean frontera. Las celdas alcanzadas son el exterior.
Las celdas no alcanzadas y no frontera son el interior.

`tryEnqueue` es el guardabarreras del BFS — garantiza que cada celda se
encola a lo sumo una vez:
```java
private void tryEnqueue(Queue<int[]> q, boolean[][] ext, boolean[][] b, int c, int r) {
    if (!b[c][r] && !ext[c][r]) { ext[c][r] = true; q.offer(new int[]{c, r}); }
}
```
Marca la celda como exterior ANTES de encolarla (no al desencolarla). Esto
evita duplicados en la cola sin necesidad de un `visited` separado.

#### Paso 3: `buildPrefix` — prefix sum 2D

```java
p[c][r] = (b[c][r] || !ext[c][r] ? 1 : 0)
         + get(p, c-1, r) + get(p, c, r-1) - get(p, c-1, r-1);
```

Fórmula estándar del **integral de imagen** (summed area table). El valor
`p[c][r]` acumula el número de celdas válidas en `[0,c]×[0,r]`. Una celda
es válida si es frontera (`b`) o no es exterior (`!ext`), es decir, es
interior o frontera.

#### Paso 4: `isValid` — consulta O(1)

```java
public boolean isValid(Tile a, Tile b) {
    int c1 = cx(Math.min(a.x(), b.x())), c2 = cx(Math.max(a.x(), b.x()));
    int r1 = cy(Math.min(a.y(), b.y())), r2 = cy(Math.max(a.y(), b.y()));
    return query(c1, r1, c2, r2) == (c2 - c1 + 1) * (r2 - r1 + 1);
}
```

La consulta de prefix sum devuelve el número de celdas válidas en el rango
comprimido. Si es igual al número total de celdas, todas son válidas.

Las coordenadas de los extremos (`a.x()`, `b.x()`) son siempre valores del
polígono, por lo que `cx(x) = 2*idx(x)` siempre devuelve un índice par
válido (resultado de `Arrays.binarySearch`).

---

## 5. Visualización del grid comprimido (ejemplo)

Teselas rojas: `(7,1), (11,1), (11,7), (9,7), (9,5), (2,5), (2,3), (7,3)`

```
xs = [2, 7, 9, 11]   →   cx: 2→0, 7→2, 9→4, 11→6
ys = [1, 3, 5, 7]    →   cy: 1→0, 3→2, 5→4, 7→6
```

Grid comprimido 7×7 (cols c=0..6, filas r=0..6):

```
     c=0     c=1     c=2     c=3     c=4     c=5     c=6
     (x=2)  (2<x<7) (x=7)  (7<x<9) (x=9)  (9<x<11) (x=11)

r=6  ·       ·       ·       ·       B       B       B    (y=7)
r=5  ·       ·       ·       EXT     B       INT     B    (5<y<7)
r=4  B       B       B       B       B       ·       B    (y=5)
r=3  B       ·       ·       INT     ·       INT     B    (3<y<5)
r=2  B       B       B       ·       ·       ·       B    (y=3)
r=1  ·       ·       B       ·       ·       ·       B    (1<y<3)
r=0  ·       ·       B       B       B       B       B    (y=1)
```

`B`=frontera, `INT`=interior (válido), `EXT`=exterior (inválido), `·`=exterior

Consulta para `(2,3)→(9,5)`: c1=0, c2=4, r1=2, r2=4 → todas las celdas son B
o INT → válido ✓ → área = `(9-2+1)×(5-3+1) = 8×3 = 24`

Consulta para `(2,5)→(11,1)`: incluye `(3,5)` que es EXT → inválido ✗

---

## 6. Principios de diseño aplicados

| Principio | Aplicación |
|-----------|-----------|
| **SRP** | `Tile` solo almacena posición; `CompressedPolygon` solo gestiona la geometría; `RectangleSolver` solo optimiza la búsqueda |
| **OCP** | `maxArea` y `maxValidArea` comparten `area()` sin modificarla; `CompressedPolygon` es independiente del solver |
| **DRY** | `area()` es privado y compartido por ambos métodos públicos de `RectangleSolver`; `get()` centraliza el acceso fuera-de-rango en el prefix sum |
| **Inmutabilidad** | `Tile` es `record`; `CompressedPolygon` construye todo en el constructor y sus campos son `final` |
| **Encapsulación** | Los 6 métodos privados de `CompressedPolygon` ocultan completamente la complejidad del algoritmo |

---

## 7. Decisiones técnicas

### ¿Por qué (2N-1) × (2N-1) y no N × N?

Con solo N × N celdas (una por cada par de coordenadas distintas), los
"huecos" entre valores consecutivos del polígono quedarían sin representar.
Un segmento horizontal de `(7,1)` a `(11,1)` necesita marcar las celdas
entre `x=7` y `x=11` (incluyendo `x=8,9,10`), no solo los dos extremos.
Las columnas impares `2i+1` representan esos huecos.

### ¿Por qué flood fill y no ray casting?

El ray casting (contar cruces de frontera) requiere una implementación
cuidadosa de los casos borde (vértices, segmentos horizontales). Para un
polígono rectilíneo, el flood fill desde el exterior es más simple y
correctamente maneja todos los casos, incluyendo polígonos no convexos
con concavidades (el ejemplo tiene una forma en "L" invertida).

### ¿Por qué marcar `ext[c][r] = true` ANTES de encolar (no al desencolar)?

Si se marca al desencolar, la misma celda puede ser encolada múltiples veces
por sus vecinos antes de ser procesada. Con la marca anticipada, una celda
se encola exactamente una vez. Esto reduce el tamaño máximo de la cola de
O(celdas²) a O(celdas).

### ¿Por qué `long` para el área en `RectangleSolver.area()`?

Con coordenadas hasta ~10^5 en el input real:
`(|x2-x1|+1) * (|y2-y1|+1) ≤ (10^5+1)² ≈ 10^10`

Esto supera `Integer.MAX_VALUE ≈ 2.1×10^9`. Sin el cast `(long)`, el
producto se calcula en `int` y desborda antes del cast, dando un resultado
incorrecto. El `(long)` debe aplicarse al PRIMER operando para forzar que
toda la multiplicación ocurra en 64 bits.

### ¿Por qué `Arrays.binarySearch` para `cx` y `cy`?

Los arreglos `xs` y `ys` están ordenados (por construcción con `sorted()`).
`Arrays.binarySearch` devuelve el índice exacto en O(log N), que luego se
multiplica por 2 para obtener la columna/fila par en el grid comprimido.
Esto es O(log N) por consulta, pero solo se llama durante la construcción
del polígono y durante las consultas de validez — en ambos casos dominado
por otras operaciones O(N) o O(1).

---

## 8. Resultados

| Parte | Respuesta |
|-------|-----------|
| Parte 1 | (input real) |
| Parte 2 | (input real) |

---

## 9. Defensa del desarrollo

**P: ¿Por qué no usar un algoritmo más sofisticado para Parte 1? Se podría
resolver en O(N log N).**

R: Con N=496 y ~123k pares, el O(N²) tarda microsegundos. La complejidad
óptima teórica sería relevante con N ≥ 100.000 (donde O(N²) tomaría
segundos). Para este input, la claridad del doble bucle supera cualquier
ganancia de rendimiento de un algoritmo más complejo.

**P: ¿El grid comprimido maneja correctamente polígonos no convexos?**

R: Sí. El flood fill desde el exterior funciona correctamente para cualquier
polígono simple (sin auto-intersecciones), convexo o no. El ejemplo del puzzle
tiene una forma en "L": la concavidad en la esquina inferior izquierda (entre
los segmentos `(9,7)→(9,5)→(2,5)`) crea una zona exterior interior al
bounding box del polígono. El flood fill la alcanza desde el borde del grid
comprimido atravesando los huecos exteriores. La visualización de la sección 5
muestra exactamente este comportamiento: `c=3, r=5` (que representa `7<x<9`
y `5<y<7`) es exterior porque el flood fill entra por `c=3, r=6` (borde
inferior del grid).

**P: ¿Hay un riesgo de StackOverflowError con el BFS recursivo?**

R: El BFS es iterativo (usa una `Queue`), no recursivo. No hay riesgo de stack
overflow independientemente del tamaño del grid. Esto es una ventaja sobre
un DFS recursivo que, con ~982k celdas, podría superar la pila de Java.

**P: ¿Qué pasaría si dos vértices del polígono tienen coordenadas adyacentes
(p. ej., x=7 y x=8)?**

R: En el grid comprimido, el "hueco" entre xs[i]=7 y xs[i+1]=8 sería la
columna 2i+1, que representa los valores estrictamente entre 7 y 8 — es decir,
ningún valor entero. Si existe un segmento entre (7,y) y (8,y), esa columna
de hueco queda marcada como frontera (correcto). Si no hay segmento que pase
por ese hueco, quedará como exterior (vacío, ningún tile real allí). La
comprobación de validez podría fallar si el rectángulo incluye ese hueco vacío
marcado como exterior. Sin embargo, en el input real las coordenadas están muy
separadas (del orden de 10^5) y este caso no ocurre.
