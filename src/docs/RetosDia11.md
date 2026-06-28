# Día 11 — Reactor

> Documentación **arquitectónica** del módulo `aoc.dia11`.  
> Visión global: [ARQUITECTURA.md](./ARQUITECTURA.md).

---

## 1. Resumen del problema

- Grafo dirigido acíclico: `nodo: dest1 dest2` por línea.
- **Parte 1:** número de caminos `you` → `out`.
- **Parte 2:** caminos `svr` → `out` que visitan **ambos** `dac` y `fft`.

---

## 2. Contrato del día

```java
public class Day11 implements Day<Graph>
```

| Parte | Delegación |
|-------|------------|
| part1 | `PathCounter.countPaths(graph)` |
| part2 | `PathCounter.countPathsThrough(graph, "dac", "fft")` |

El modelo parseado es el **grafo de dominio**, no un `Map` crudo.

---

## 3. Estructura de paquetes

```
aoc.dia11/
├── Day11.java
├── Parser.java
└── model/
    ├── Graph.java         record
    └── PathCounter.java
```

---

## 4. Catálogo de clases

| Clase | Rol | API principal | Depende de |
|-------|-----|---------------|------------|
| **Day11** | Orquestador | `parse`, `part1`, `part2` | `Parser`, `PathCounter` |
| **Parser** | Líneas → adyacencias | `parse(String)` → `Graph` | `Lines` |
| **Graph** | VO inmutable del DAG | `neighbors(node)` | `Map` interno |
| **PathCounter** | DFS + memoización | `countPaths`, `countPathsThrough` | `Graph` |

### `PathCounter` — dos modos

| Método | Estado memoizado | Condición base |
|--------|------------------|----------------|
| `countPaths` | `Map<String, Long>` por nodo | `out` → 1 |
| `countPathsThrough` | `Map<String, long[4]>` nodo × máscara 0–3 | `out` y máscara == 3 → 1 |

La máscara codifica si ya se visitó `dac` (bit 1) y/o `fft` (bit 2).

---

## 5. Colaboración entre clases

```
Parser → Graph(adjacency)
PathCounter → dfs desde nodo origen
  └─ for next in graph.neighbors(node): acumular dfs(next)
```

`Day11` no conoce la estructura del mapa; solo pasa `Graph` al contador.

---

## 6. Decisiones de este día

| Decisión | Motivo |
|----------|--------|
| Record `Graph` (Fase 3 del plan) | Sustituir `Map<String,List<String>>` crudo por API de dominio |
| `neighbors` con lista vacía por defecto | Evitar null checks en el DFS |
| Parte 2 con máscara de bits | Solo 2 nodos obligatorios → 4 estados por nodo, memo compacto |
| Sin subpaquete extra | 2 clases de modelo cohesivas; fichero manejable |

---

## 7. Patrones

- **Value Object:** `Graph`.
- **Memoization** (técnica algorítmica en `PathCounter`).

---

## 8. Dependencias compartidas

- `aoc.parse.Lines`
- `aoc.core.Day`

---

## 9. Resultados verificados

- Parte 1: `714`
- Parte 2: `333852915427200`
