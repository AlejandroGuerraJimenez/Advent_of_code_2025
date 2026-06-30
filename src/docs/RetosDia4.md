# Día 4 — Printing Department

> Documentación **arquitectónica** del módulo `aoc.dia4`.  
> Visión global: [ARQUITECTURA.md](./ARQUITECTURA.md).

---

## 1. Resumen del problema

- Rejilla de `@` (rollos) y `.` (vacío).
- **Parte 1:** contar `@` con **menos de 4** vecinos `@` (8 direcciones).
- **Parte 2:** iterar: quitar accesibles hasta estabilizar; contar cuántos se eliminaron en total.

---

## 2. Contrato del día

```java
public class Day04 implements Day<TextGrid>
```

El **modelo parseado es directamente** `TextGrid` (utilidad compartida); no hay VO intermedio del día 4.

| Parte | Delegación |
|-------|------------|
| part1 | `ForkliftAccessChecker.countAccessible(grid)` |
| part2 | `ForkliftAccessChecker.countRemovable(grid)` |

---

## 3. Estructura de paquetes

```
aoc.dia4/
├── Day04.java
├── Parser.java
└── model/
    └── ForkliftAccessChecker.java
```

*(Eliminado `Grid` local — sustituido por `aoc.parse.TextGrid`.)*

---

## 4. Catálogo de clases

| Clase | Rol | API principal | Depende de |
|-------|-----|---------------|------------|
| **Day04** | Orquestador delgado | `parse` → `TextGrid`; delega conteos | `Parser`, `ForkliftAccessChecker` |
| **Parser** | Texto → `TextGrid` | `parse(String)` | `TextGrid.fromLines`, `Lines` |
| **ForkliftAccessChecker** | Reglas de accesibilidad y simulación iterativa | `countAccessible`, `countRemovable` | `TextGrid` |

**Parte 1:** snapshot inmutable — solo lectura de la rejilla.  
**Parte 2:** copia mutable de caracteres; bucle quitar-accesibles hasta fijpoint.

---

## 5. Modelo de clases UML

Diagrama de clases del módulo `aoc.dia4` y el tipo parseado `TextGrid` (shared kernel). Notación UML 2.5 (misma convención que días 1–3):

- Visibilidad (`+`/`-`): **solo** dentro de cada caja; las flechas no llevan `+`/`-`.
- **`<<utility>>`**: sustituye repetir `{static}` en cada método.
- Solo **dependencias** (`..>`) con multiplicidad; un único `TextGrid` por ejecución (`1` : `1`).
- No se incluyen `Day`, `Lines`, `List`, `String`, ni `char[][]`.

**`TextGrid`.** Record compartido con el día 7. En la caja solo primitivos visibles (`+width`, `+empty`) y operaciones de consulta; el campo `rows` (`List<String>`) no se modela (tipo JDK). `+fromLines` es factory del kernel.

**Parte 1 vs parte 2.** Mismo `TextGrid` inmutable en entrada. `countRemovable` copia a `char[][]` internamente; esa copia no aparece en el diagrama.

```mermaid
classDiagram
    direction TB

    namespace aoc.dia4 {
        class Day04 {
            +number() int
            +parse(input String) TextGrid
            +part1(grid TextGrid) Object
            +part2(grid TextGrid) Object
        }

        class Parser {
            <<utility>>
            +parse(input String) TextGrid
        }
    }

    namespace aoc.dia4.model {
        class ForkliftAccessChecker {
            <<utility>>
            +countAccessible(grid TextGrid) long
            +countRemovable(grid TextGrid) long
        }
    }

    namespace aoc.parse {
        class TextGrid {
            <<record>>
            +width int
            +empty char
            +fromLines(rows List~String~) TextGrid
            +height() int
            +at(row int, col int) char
            +inBounds(row int, col int) boolean
        }
    }

    Day04 "1" ..> "1" Parser
    Day04 "1" ..> "1" TextGrid
    Day04 "1" ..> "1" ForkliftAccessChecker
    Parser "1" ..> "1" TextGrid
    ForkliftAccessChecker "1" ..> "1" TextGrid
```

| Relación | Multiplicidad | Motivo en el código |
|----------|---------------|---------------------|
| `Day04` → `Parser` | `1` : `1` | `parse` delega en `Parser`. |
| `Day04` → `TextGrid` | `1` : `1` | Un único grid parseado para ambas partes. |
| `Day04` → `ForkliftAccessChecker` | `1` : `1` | `part1` / `part2` delegan en métodos distintos. |
| `Parser` → `TextGrid` | `1` : `1` | Cada `parse` construye un `TextGrid`. |
| `ForkliftAccessChecker` → `TextGrid` | `1` : `1` | Cada método público recibe un grid (solo lectura; parte 2 copia dentro). |

---

## 6. Colaboración entre clases

```mermaid
flowchart LR
    Parser --> TextGrid
    Day04 --> ForkliftAccessChecker
    TextGrid --> ForkliftAccessChecker
```

`Day04` no accede a celdas directamente: toda la semántica `@`/vecinos vive en `ForkliftAccessChecker`.

---

## 7. Decisiones de este día

| Decisión | Motivo |
|----------|--------|
| Modelo = `TextGrid` compartido | Misma abstracción que día 7; evita `Grid` duplicado |
| Checker separado del parseo | Formato del input ≠ reglas de accesibilidad |
| Parte 2 con grid mutable interno | La simulación modifica estado; no mutar el `TextGrid` parseado |

---

## 8. Patrones

- **Reutilización de infraestructura:** `TextGrid` como modelo.
- **Servicio de dominio estático:** `ForkliftAccessChecker` sin estado de instancia.

---

## 9. Dependencias compartidas

- `aoc.parse.TextGrid`, `Lines`
- `aoc.core.Day`
