# Día 5 — Cafeteria

> Documentación **arquitectónica** del módulo `aoc.dia5`.  
> Visión global: [ARQUITECTURA.md](./ARQUITECTURA.md).

---

## 1. Resumen del problema

- Input en **dos secciones** (línea en blanco): rangos de frescura e IDs de ingredientes.
- **Parte 1:** cuántos IDs caen en algún rango fresco.
- **Parte 2:** cuántos enteros distintos cubren la unión de rangos (rangos fusionados).

---

## 2. Contrato del día

```java
public class Day05 implements Day<IngredientDatabase>
```

| Parte | Método de dominio |
|-------|-------------------|
| part1 | `FreshnessChecker.countFresh(database)` |
| part2 | `FreshnessChecker.countAllFresh(database)` |

El modelo agrupa rangos e IDs en un solo VO de entrada.

---

## 3. Estructura de paquetes

```
aoc.dia5/
├── Day05.java
├── Parser.java
└── model/
    ├── IngredientDatabase.java   record
    └── FreshnessChecker.java
```

*(Eliminado `FreshRange` — usa `aoc.parse.LongRange`.)*

---

## 4. Catálogo de clases

| Clase | Rol | API principal | Depende de |
|-------|-----|---------------|------------|
| **Day05** | Orquestador | `parse`, `part1`, `part2` | `Parser`, `FreshnessChecker` |
| **Parser** | Dos secciones → `IngredientDatabase` | `parse(String)` | `Sections`, `LongRange` |
| **IngredientDatabase** | VO: rangos + lista de IDs | `freshRanges()`, `ingredientIds()` | `LongRange` |
| **FreshnessChecker** | Consultas de frescura y fusión de rangos | `countFresh`, `countAllFresh` | `IngredientDatabase`, `LongRange` |

**`countAllFresh`:** ordena rangos, fusiona adyacentes/solapados con `connectsWith` + `union`, suma `length()`.

---

## 5. Colaboración entre clases

```
Parser
  ├─ Sections.split(input) → [bloque rangos, bloque IDs]
  └─ IngredientDatabase(ranges, ids)

Day05.part1 → FreshnessChecker.countFresh(db)
  └─ stream ids.filter(id → any range.contains(id))

Day05.part2 → FreshnessChecker.countAllFresh(db)
  └─ mergeRanges → suma longitudes
```

`Day05` no expone `freshRanges()` al exterior; la parte 2 no filtra IDs sueltos.

---

## 6. Decisiones de este día

| Decisión | Motivo |
|----------|--------|
| `IngredientDatabase` como record compuesto | Un solo objeto parseado para ambas partes |
| `countAllFresh(IngredientDatabase)` vs filtrar en `Day05` | Encapsular fusión de rangos en el dominio |
| `LongRange.connectsWith` / `union` en `aoc.parse` | Comportamiento reutilizable del value object |

---

## 7. Patrones

- **Value Object:** `IngredientDatabase`, `LongRange`.
- **Facade de datos:** el record agrupa lo que el parser produce.

---

## 8. Dependencias compartidas

- `aoc.parse.Sections`, `LongRange`
- `aoc.core.Day`
