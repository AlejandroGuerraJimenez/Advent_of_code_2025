# Reto Día 8 — Playground

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 8 de Advent of Code 2025.

---

## 1. El problema

Los Elfos tienen N cajas de empalme en el espacio 3D. Cada par de cajas puede
conectarse con una cadena de luces. Cuando dos cajas están conectadas
(directamente o a través de otras), forman parte del mismo **circuito**. Se
quiere conectar primero los pares más cercanos (distancia euclidiana).

### Parte 1 — Producto de los tres circuitos más grandes tras 1000 conexiones

Conectar las **1000 parejas más cercanas** entre sí (aunque algunas ya estén en
el mismo circuito, no pasa nada). Después, obtener los tamaños de todos los
circuitos resultantes y multiplicar los tres más grandes.

Ejemplo (20 cajas, 10 conexiones): circuitos de tamaño 5, 4, 2, 2 y 7×1 →
`5 × 4 × 2 = 40`

### Parte 2 — Producto de las X del último par que unifica todo

Seguir conectando (por orden de distancia) hasta que **todas las cajas formen
un único circuito**. El resultado es el producto de las coordenadas X de las
dos cajas de ese último par.

Ejemplo: la última conexión que unifica las 20 cajas es entre `216,146,977`
y `117,168,530` → `216 × 117 = 25272`

---

## 2. Estructura de paquetes

```
aoc.dia8
├── Day08.java              ← Entry point del día
├── Parser.java             ← Convierte líneas "X,Y,Z" en Point3D
└── model/
    ├── Point3D.java         record(x,y,z) + distSq()
    ├── Edge.java            record(a,b,distSq) + Comparable
    ├── CircuitBoard.java    Union-Find con path compression y union by rank
    └── CircuitAnalyzer.java topThreeProduct() y lastPairXProduct()
```

---

## 3. Flujo de ejecución

### Parte 1
```
input (String)
  → Parser.parse()                    lista de Point3D
  → CircuitAnalyzer.topThreeProduct(points, 1000)
      allEdges(): N*(N-1)/2 pares con distSq
      sorted() → limit(1000)
      forEach: board.union(a, b)      Union-Find
      circuitSizes() → sort desc
      sizes[0] * sizes[1] * sizes[2]
  → long
```

### Parte 2
```
input (String)
  → Parser.parse()
  → CircuitAnalyzer.lastPairXProduct(points)
      allEdges() → sorted() → toList()
      for each edge:
        board.union(a, b) → true si fusionó
        board.components() == 1 → todos unidos
        → return points[a].x() * points[b].x()
  → long
```

---

## 4. Explicación clase a clase

### `Point3D` — record
```java
public record Point3D(int x, int y, int z) {
    public long distSq(Point3D o) {
        long dx = x - o.x, dy = y - o.y, dz = z - o.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
```
Value object inmutable para coordenadas 3D. `distSq` devuelve la **distancia
al cuadrado** como `long` para evitar overflow (máx. `3 × 1000² = 3_000_000`,
dentro del rango `int`, pero el producto de tres deltas como `long` previene
desbordamiento en coordenadas grandes) y para evitar `Math.sqrt()`, que es
innecesario para comparar distancias.

### `Edge` — record + Comparable
```java
public record Edge(int a, int b, long distSq) implements Comparable<Edge> {
    @Override
    public int compareTo(Edge o) { return Long.compare(distSq, o.distSq); }
}
```
Representa un par de índices de puntos y su distancia al cuadrado. Implementa
`Comparable` para que `Stream.sorted()` funcione sin comparador externo. Los
índices `a` y `b` son posiciones en la lista de `Point3D`, no los puntos en sí,
lo que evita duplicar objetos.

### `CircuitBoard` — Union-Find con dos optimizaciones clásicas

Es el núcleo algorítmico del día. Implementa la estructura de datos
**Disjoint Set Union (DSU)** con:

**Path compression** en `find()`:
```java
public int find(int i) {
    if (parent[i] != i) parent[i] = find(parent[i]);
    return parent[i];
}
```
Cuando se sube recursivamente hasta la raíz, se acorta el puntero de cada
nodo visitado directamente a la raíz. Las consultas futuras desde esos nodos
son O(1). Sin esta optimización, un árbol degenerado (cadena lineal) haría
`find()` en O(n).

**Union by rank** en `union()`:
```java
public boolean union(int a, int b) {
    int ra = find(a), rb = find(b);
    if (ra == rb) return false;
    if (rank[ra] >= rank[rb]) { parent[rb] = ra; if (rank[ra] == rank[rb]) rank[ra]++; }
    else parent[ra] = rb;
    components--;
    return true;
}
```
Siempre se adjunta el árbol de menor rango bajo el de mayor rango. Esto
garantiza que la profundidad del árbol nunca supere `log₂(n)`. El `rank` solo
se incrementa cuando ambos tienen el mismo (merge de árboles iguales).

El método devuelve `boolean`: `true` si hubo fusión real, `false` si `a` y `b`
ya estaban en el mismo circuito. Esto permite a los consumidores reaccionar a
fusiones reales sin llamadas extra.

**Contador de componentes:**
```java
private int components;  // empieza en n, --  por cada union real
public int components() { return components; }
```
En lugar de recorrer todo el array para saber si quedan circuitos, se mantiene
un contador O(1). Es la clave para que la Parte 2 sea eficiente: el bucle para
en cuanto `components == 1` sin ninguna verificación adicional.

### `CircuitAnalyzer` — algoritmo principal
Orquesta la generación de aristas, el ordenado y la aplicación del Union-Find.

**Generación de aristas:**
```java
private static List<Edge> allEdges(List<Point3D> pts) {
    List<Edge> edges = new ArrayList<>();
    for (int i = 0; i < pts.size(); i++)
        for (int j = i + 1; j < pts.size(); j++)
            edges.add(new Edge(i, j, pts.get(i).distSq(pts.get(j))));
    return edges;
}
```
Con N=1000 puntos: `1000×999/2 = 499_500` aristas. Completamente manejable
en memoria y tiempo. El doble bucle evita aristas duplicadas (solo `i < j`).

**Parte 1:**
```java
public static long topThreeProduct(List<Point3D> points, int connections) {
    CircuitBoard board = new CircuitBoard(points.size());
    allEdges(points).stream().sorted().limit(connections).forEach(e -> board.union(e.a(), e.b()));
    List<Integer> sizes = board.circuitSizes();
    sizes.sort(Comparator.reverseOrder());
    if (sizes.size() < 3) throw new IllegalStateException(...);
    return (long) sizes.get(0) * sizes.get(1) * sizes.get(2);
}
```
`Stream.sorted()` sobre `Edge` (que implementa `Comparable`) ordena las
~500k aristas. `limit(1000)` descarta el resto antes de procesarlas.
La excepción defensiva protege el acceso a `sizes.get(1)` y `sizes.get(2)`
cuando hay menos de 3 circuitos (ocurre si el input tiene pocas cajas).

**Parte 2:**
```java
public static long lastPairXProduct(List<Point3D> points) {
    CircuitBoard board = new CircuitBoard(points.size());
    for (Edge e : allEdges(points).stream().sorted().toList())
        if (board.union(e.a(), e.b()) && board.components() == 1)
            return (long) points.get(e.a()).x() * points.get(e.b()).x();
    throw new IllegalStateException("No se pudo conectar todos los circuitos");
}
```
El cortocircuito de `&&` es crucial: si `union()` devuelve `false` (ya estaban
conectados), nunca se evalúa `components() == 1`, evitando una llamada
innecesaria. El método retorna en el momento exacto en que todos los nodos
se unifican.

---

## 5. Principios de diseño aplicados

| Principio | Aplicación |
|-----------|-----------|
| **SRP** | `CircuitBoard` gestiona el estado DSU, `CircuitAnalyzer` orquesta el algoritmo, `Parser` parsea, `Edge`/`Point3D` son datos |
| **Inmutabilidad** | `Point3D` y `Edge` son `record` — nunca se modifican una vez creados |
| **OCP** | `CircuitAnalyzer` trabaja con cualquier número de conexiones gracias al parámetro `connections`; `lastPairXProduct` reutiliza `allEdges` sin modificarla |
| **DRY** | `allEdges()` es privado y compartido por ambos métodos públicos — no hay duplicación de la generación de pares |
| **Fail fast** | La excepción explícita en `topThreeProduct` da un mensaje claro si el input tiene pocos puntos, en vez de `IndexOutOfBoundsException` opaco |

---

## 6. Decisiones técnicas

### ¿Por qué distancia al cuadrado y no distancia euclidiana?

Para ordenar solo se necesita comparar: `distSq(a,b) < distSq(c,d)` si y solo
si `dist(a,b) < dist(c,d)`. Evitamos:
- `Math.sqrt()` (operación de punto flotante costosa)
- Errores de redondeo (dos distancias muy parecidas podrían empatar con `double`
  pero diferir en `long`, dando un orden determinista)

### ¿Por qué indices en `Edge` en vez de referencias a `Point3D`?

Guardar índices `int a, int b` permite:
- Calcular `distSq` una sola vez al crear el `Edge`
- En Parte 2, recuperar `points.get(e.a()).x()` directamente sin buscar el objeto
- Menor uso de memoria que guardar dos referencias a objetos

### ¿Por qué `components` en `CircuitBoard` en lugar de recalcular?

`circuitSizes()` requiere iterar todo el array `parent` (O(n)) para contar
componentes. Con N=1000 y ~500k aristas, recalcular en cada iteración del
bucle de Parte 2 sería O(n × aristas) = O(500M operaciones). El contador
entero reduce ese coste a O(1) por arista.

### ¿Por qué `union()` devuelve `boolean`?

Para que el llamador pueda distinguir entre "fusión real" (dos circuitos
distintos se unieron) e "idempotente" (ya estaban conectados). En Parte 2,
comprobar `components() == 1` solo tiene sentido después de una fusión real:
si `union()` devuelve `false`, `components` no cambió y no podemos haber
llegado a 1. El cortocircuito `&&` explota esta garantía.

### ¿Por qué `allEdges()` crea todos los pares y no usa un grafo explícito?

Con N=1000, los pares son ~500k. El problema no tiene estructura de grafo
preexistente — cada par de puntos puede conectarse. Un grafo explícito con
listas de adyacencia no aportaría nada: de todas formas habría que generar
y ordenar todas las aristas para el algoritmo de Kruskal.

---

## 7. Resultados

| Parte | Respuesta |
|-------|-----------|
| Parte 1 | (input real) |
| Parte 2 | (input real) |

---

## 8. Contexto algorítmico: Kruskal sin MST completo

El algoritmo de Parte 1 es **Kruskal truncado**: el algoritmo de Kruskal para
el árbol de expansión mínima (MST) procesa aristas por orden de distancia y
las acepta si conectan componentes distintos. Aquí se diferencian dos variantes:

- **Parte 1**: se procesan las 1000 aristas más cortas **sin descartar** las que
  ya conectan el mismo circuito (el enunciado dice "connect the 1000 pairs",
  no "connect 1000 new pairs"). Esto es una variante laxa de Kruskal.

- **Parte 2**: equivale al **MST completo de Kruskal** — se procesan aristas en
  orden hasta que todos los nodos están en un solo componente. La diferencia
  con el MST estándar es que aquí nos interesa la **última** arista del MST,
  no el árbol completo.

La complejidad es `O(N² log N²) = O(N² log N)` para generar y ordenar las
aristas, dominando sobre el `O(N² α(N))` del Union-Find (donde `α` es la
función inversa de Ackermann, prácticamente constante). Con N=1000: ~500k
aristas, ordenadas en milisegundos.

---

## 9. Defensa del desarrollo

**P: ¿Por qué no usar un algoritmo de vecino más cercano (k-d tree) para
evitar generar todas las aristas?**

R: Un k-d tree reduciría la búsqueda de vecinos de O(N²) a O(N log N), pero
el problema requiere exactamente las K aristas globalmente más cortas, no los
K vecinos de un nodo dado. Con N=1000 y ~500k aristas, la generación directa
es viable y más simple. Un k-d tree solo sería necesario con N >> 10.000.

**P: ¿Por qué `union by rank` y no `union by size`?**

R: Ambos garantizan O(log N) de profundidad y `O(α(N))` amortizado con path
compression. `rank` es ligeramente más simple de implementar (no hace falta
mantener tamaños) y es el estándar en la literatura académica. Con N=1000,
la diferencia práctica es imperceptible.

**P: ¿La Parte 2 podría devolver un resultado incorrecto si hay múltiples
aristas de la misma distancia que causan la última fusión?**

R: El enunciado garantiza que hay una única última fusión (la que lleva de 2
componentes a 1). Si dos aristas tuvieran exactamente la misma distancia y
ambas fueran candidatas a ser "la última", el orden entre ellas sería
arbitrario pero consistente (determinista dado el input). El enunciado del
puzzle tiene una respuesta única, lo que implica que no hay empate en esa
última distancia.

**P: ¿Por qué `IllegalStateException` y no devolver `-1` como error?**

R: Devolver `-1` o `0` como señal de error es un antipatrón: obliga al
llamador a comprobar el valor de retorno y hace que un error silencioso se
propague como respuesta incorrecta. Una excepción detiene la ejecución
inmediatamente y da un mensaje descriptivo del problema real. Seguir el
principio de "fail fast" es mejor que propagar valores inválidos.
