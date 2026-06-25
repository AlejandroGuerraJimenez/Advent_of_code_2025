# Reto Día 11 — Reactor

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 11 de Advent of Code 2025.

---

## 1. El problema

La entrada describe una red de **dispositivos** y sus salidas. Cada línea
`bbb: ddd eee` significa que el dispositivo `bbb` tiene dos salidas dirigidas:
una hacia `ddd` y otra hacia `eee`. Los datos **solo fluyen hacia adelante**
(de un dispositivo a sus salidas), nunca hacia atrás → es un **grafo dirigido
acíclico (DAG)**.

### Parte 1 — Contar caminos `you → out`

Hay que contar **cuántos caminos distintos** llevan del dispositivo `you` al
dispositivo `out`.

Ejemplo:

```
aaa: you hhh
you: bbb ccc
bbb: ddd eee
ccc: ddd eee fff
ddd: ggg
eee: out
fff: out
ggg: out
hhh: ccc fff iii
iii: out
```

Resultado del ejemplo → **5**.

### Parte 2 — Caminos `svr → out` que pasan por `dac` **y** `fft`

Ahora se cuentan los caminos de `svr` a `out` que **visiten ambos** nodos
`dac` y `fft` (en cualquier orden).

Ejemplo → **2**.

---

## 2. Estructura de paquetes

```
aoc.dia11
├── Day11.java          ← Entry point del día
├── Parser.java         ← Parsea el texto en un Map<String, List<String>>
└── model/
    └── PathCounter.java  DFS + memoización (Parte 1 y Parte 2)
```

---

## 3. Flujo de ejecución

### Parte 1 — DFS con memoización

```
input (String)
  → Parser.parse()                Map<nodo, salidas>
  → PathCounter.countPaths()
       dfs(node):
         if node == "out" → 1
         memo[node] = Σ dfs(next)  ∀ next ∈ salidas(node)
  → String (respuesta Parte 1)
```

### Parte 2 — DFS con estado (nodo, máscara)

```
input (String)
  → Parser.parse()                Map<nodo, salidas>
  → PathCounter.countPathsThrough(graph, "dac", "fft")
       bits = { dac→1, fft→2 }
       dfs(node, mask):
         if node == "out" → (mask == 3) ? 1 : 0
         memo[node][mask] = Σ dfs(next, mask | bits[next])
  → String (respuesta Parte 2)
```

---

## 4. Explicación clase a clase

### `Parser` — texto a grafo

Cada línea `clave: v1 v2 v3` se convierte en una entrada del mapa
`clave → [v1, v2, v3]`:

```java
public static Map<String, List<String>> parse(String input) {
    return input.lines()
            .filter(l -> !l.isBlank())
            .collect(Collectors.toMap(
                    l -> l.substring(0, l.indexOf(':')),
                    l -> Arrays.asList(l.substring(l.indexOf(':') + 2).split(" "))
            ));
}
```

Los nodos hoja como `out` no aparecen como clave; se tratan con
`getOrDefault(node, List.of())` durante el recorrido.

---

### `PathCounter` — conteo de caminos

#### Parte 1: memoización por nodo

Como es un DAG, el número de caminos desde un nodo hasta `out` **no depende del
camino recorrido para llegar a él**. Por eso se puede cachear `dp[node]` y
reutilizarlo en cada nodo que tenga varias entradas.

```java
private static long dfs(String node, Map<String, List<String>> graph, Map<String, Long> memo) {
    if ("out".equals(node)) return 1;
    if (memo.containsKey(node)) return memo.get(node);
    long count = 0;
    for (String next : graph.getOrDefault(node, List.of()))
        count += dfs(next, graph, memo);
    memo.put(node, count);
    return count;
}
```

- **Complejidad**: O(V + E). Cada nodo se calcula una sola vez.
- **Por qué `long`**: el número de caminos crece de forma exponencial en redes
  densas (el resultado real supera los 10¹⁴).

#### Parte 2: extensión del estado con una máscara de bits

La condición "visitar `dac` y `fft`" se añade al estado de la memoización con
una **máscara de 2 bits**:

| bit | significado |
|-----|-------------|
| `0b01` | ya se pasó por `dac` |
| `0b10` | ya se pasó por `fft` |

El estado pasa de `node` a `(node, mask)`. Un camino solo cuenta si llega a
`out` con `mask == 0b11`.

```java
private static long dfs2(String node, Map<String, Integer> bits,
                          Map<String, List<String>> graph,
                          Map<String, long[]> memo, int mask) {
    if ("out".equals(node)) return mask == 3 ? 1 : 0;
    long[] cached = memo.get(node);
    if (cached != null && cached[mask] >= 0) return cached[mask];
    if (cached == null) {
        cached = new long[]{-1, -1, -1, -1};
        memo.put(node, cached);
    }
    long count = 0;
    for (String next : graph.getOrDefault(node, List.of())) {
        int newMask = mask | bits.getOrDefault(next, 0);
        count += dfs2(next, bits, graph, memo, newMask);
    }
    cached[mask] = count;
    return count;
}
```

El caché por nodo es un `long[4]` (4 máscaras posibles) inicializado a `-1`
como centinela de "no calculado". Es más rápido y ligero que un mapa anidado.

---

## 5. Decisiones de diseño

### Por qué memoización y no fuerza bruta

Sin memoización, contar caminos en un DAG es exponencial: una rejilla de
diamante de N niveles tiene 2^N caminos. La memoización reduce el coste a lineal
porque comprime todos esos caminos en un único valor por nodo.

### Por qué la máscara en el estado (Parte 2)

Si se intentara contar "caminos que pasan por A y B" con inclusión-exclusión
(`total − sin_A − sin_B + sin_ambos`), habría que repetir cuatro recorridos.
Incluir la máscara en el estado lo resuelve en **un solo recorrido** y mantiene
la memoización válida: dos llegadas al mismo nodo con la misma máscara producen
exactamente el mismo subconteo.

### Reutilización del modelo

Ambas partes comparten el mismo `Map<nodo, salidas>` producido por `Parser`.
La diferencia está solo en la función de conteo, no en la representación.

---

## 6. Resultados

| Parte | Respuesta | Algoritmo | Complejidad |
|-------|-----------|-----------|-------------|
| 1 | **714** | DFS + memoización | O(V + E) |
| 2 | **333852915427200** | DFS + memoización con máscara | O((V + E) · 4) |

---

## Mejoras arquitectónicas aplicadas

### Fase 1 — Core: interfaz `Day<T>` con parseo único
- `Day11` implementa ahora `Day<Map<String, List<String>>>`: el grafo se **parsea
  una sola vez** y ambas partes operan sobre él.
- `part1`/`part2` devuelven `Object`; el `toString` lo hace `DayRunner`.
- Se añadió `number()`. La salida muestra la etiqueta y el resultado de cada parte.
### Fase 3 — Value object de dominio (`Graph`)
- **Nuevo record `Graph`** que envuelve el `Map<String, List<String>>` y expone
  `neighbors(node)`. `Day11` es ahora `Day<Graph>` y `PathCounter` opera sobre
  `Graph` en vez de pasar el `Map`/`List` crudos por todos los métodos.
- `Parser` migrado a `Lines.nonBlank` y comentarios traducidos al español.
