# Día 6 — Workbook

> Documentación **arquitectónica** del módulo `aoc.dia6`.  
> Visión global: [ARQUITECTURA.md](./ARQUITECTURA.md).

---

## 1. Resumen del problema

- Misma entrada física, **dos lecturas** distintas del layout numérico.
- **Parte 1:** problemas en filas (operador al final de cada fila de números).
- **Parte 2:** problemas en columnas leídas de derecha a izquierda.
- Respuesta: suma de resultados de todos los problemas.

---

## 2. Contrato del día

```java
public class Day06 implements Day<Worksheets>
```

```java
public record Worksheets(MathWorksheet horizontal, MathWorksheet vertical) {}
```

| Fase | Acción |
|------|--------|
| `parse` | Construye **ambas** hojas: `Parser.parse` + `Parser.parseVertical` |
| part1 | `WorksheetSolver.grandTotal(horizontal)` |
| part2 | `WorksheetSolver.grandTotal(vertical)` |

Un solo parseo produce las dos vistas; evita releer el string.

---

## 3. Estructura de paquetes

```
aoc.dia6/
├── Day06.java
├── Parser.java
└── model/
    ├── Worksheets.java      record contenedor
    ├── MathWorksheet.java   record(List Problem)
    ├── Problem.java         record(numbers, operator)
    └── WorksheetSolver.java
```

---

## 4. Catálogo de clases

| Clase | Rol | API principal | Depende de |
|-------|-----|---------------|------------|
| **Day06** | Orquestador; compone `Worksheets` en parse | `parse`, `part1`, `part2` | `Parser`, `WorksheetSolver` |
| **Parser** | Lectura horizontal y vertical | `parse`, `parseVertical` | `Lines`, columnas del input |
| **Worksheets** | VO compuesto: dos hojas del mismo input | record | `MathWorksheet` |
| **MathWorksheet** | Lista de problemas | `problems()` | `Problem` |
| **Problem** | Números + operador `+` o `*` | record | — |
| **WorksheetSolver** | Evalúa y suma problemas | `grandTotal(MathWorksheet)` | `Problem` |

---

## 5. Colaboración entre clases

```mermaid
flowchart TB
    Input[String input]
    Input --> ParserH[Parser.parse]
    Input --> ParserV[Parser.parseVertical]
    ParserH --> WS[Worksheets]
    ParserV --> WS
    WS --> Day06
    Day06 -->|part1| Solver[WorksheetSolver]
    Day06 -->|part2| Solver
```

El solver es **idéntico** para ambas partes; solo cambia qué `MathWorksheet` recibe.

---

## 6. Decisiones de este día

| Decisión | Motivo |
|----------|--------|
| Record `Worksheets` | Modelar explícitamente que parte 1 y 2 comparten input pero no layout |
| Dos métodos en `Parser`, no flags | Cada lectura tiene lógica distinta; nombres claros |
| `WorksheetSolver` agnóstico de orientación | Una sola implementación de `+`/`*` |

---

## 7. Patrones

- **Composite ligero:** `Worksheets` agrupa dos hojas.
- **Value Object:** records anidados inmutables.

---

## 8. Dependencias compartidas

- `aoc.parse.Lines`
- `aoc.core.Day`
