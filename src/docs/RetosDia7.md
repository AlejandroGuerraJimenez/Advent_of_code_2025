# Día 7 — Laboratories

> Documentación **arquitectónica** del módulo `aoc.dia7`.  
> Visión global: [ARQUITECTURA.md](./ARQUITECTURA.md).

---

## 1. Resumen del problema

- Rejilla con `S` (origen), `.` (vacío) y `^` (splitter).
- Tachyon baja en columna; al chocar con `^` se bifurca izquierda/derecha.
- **Parte 1:** contar splits (bifurcaciones).
- **Parte 2:** contar timelines (camino con multiplicidad / conteo de rutas).

---

## 2. Contrato del día

```java
public class Day07 implements Day<Manifold>
```

| Parte | Delegación |
|-------|------------|
| part1 | `TachyonSimulator.countSplits(manifold)` |
| part2 | `TachyonSimulator.countTimelines(manifold)` |

---

## 3. Estructura de paquetes

```
aoc.dia7/
├── Day07.java
├── Parser.java
└── model/
    ├── Manifold.java       record — adapter sobre TextGrid
    ├── Position.java       record
    └── TachyonSimulator.java
```

---

## 4. Catálogo de clases

| Clase | Rol | API principal | Depende de |
|-------|-----|---------------|------------|
| **Day07** | Orquestador | `parse`, `part1`, `part2` | `Parser`, `TachyonSimulator` |
| **Parser** | Líneas → `Manifold` | `parse(String)` | `TextGrid`, `Lines` |
| **Manifold** | **Adapter:** rejilla + API con `Position` + búsqueda de `S` | `at(Position)`, `start()`, `inBounds` | `TextGrid`, `Position` |
| **Position** | Celda `(row, col)` + clave para `Set` | `key(width)` | — |
| **TachyonSimulator** | Simulación BFS / conteo de rutas | `countSplits`, `countTimelines` | `Manifold`, `Position` |

---

## 5. Colaboración entre clases

```
Parser → TextGrid.fromLines → new Manifold(grid)
TachyonSimulator → usa Manifold (no TextGrid directo)
  ├─ start() localiza 'S'
  ├─ BFS con Queue Position + visited
  └─ part2: memoización / multiplicidad de haces
```

La semántica del día 7 habla en **posiciones nombradas**; `Manifold` traduce al grid genérico.

---

## 6. Decisiones de este día

| Decisión | Motivo |
|----------|--------|
| `Manifold` envuelve `TextGrid` (Adapter) | Reutilizar parseo de rejilla del día 4 sin duplicar `Grid` |
| `Position` propio del día 7 | No unificar con `Tile` (día 9): fila/columna vs x/y del enunciado |
| Simulador separado de `Manifold` | El manifold es el *espacio*; el simulador es el *algoritmo* |

---

## 7. Patrones

- **Adapter:** `Manifold` → `TextGrid`.
- **Value Object:** `Position` (record).

---

## 8. Dependencias compartidas

- `aoc.parse.TextGrid`, `Lines`
- `aoc.core.Day`
