# Día 10 — Factory

> Documentación **arquitectónica** del módulo `aoc.dia10`.  
> Visión global: [ARQUITECTURA.md](./ARQUITECTURA.md).

---

## 1. Resumen del problema

- Cada línea describe una **máquina**: diagrama de luces, botones `(i,j,…)`, joltages `{…}`.
- **Parte 1:** botones alternan luces (XOR); mínimo de pulsaciones por máquina (BFS bitmask).
- **Parte 2:** botones incrementan contadores; alcanzar joltages exactos (ILP entero mínimo).

---

## 2. Contrato del día

```java
public class Day10 implements Day<List<Machine>>
```

| Parte | Delegación | Agregación |
|-------|------------|------------|
| part1 | `ButtonSolver.minPresses` | `stream().mapToInt().sum()` |
| part2 | `JoltageSolver.minPresses` | idem |

---

## 3. Estructura de paquetes

```
aoc.dia10/
├── Day10.java
├── Parser.java
├── optimization/               ← LP exacto (enriquecimiento local)
│   ├── Rational.java
│   ├── SimplexSolver.java
│   └── BranchAndBoundSolver.java
└── model/
    ├── Machine.java            record
    ├── ButtonSolver.java       BFS parte 1
    └── JoltageSolver.java      fachada parte 2
```

---

## 4. Catálogo de clases

### Orquestación y parseo

| Clase | Rol | API |
|-------|-----|-----|
| **Day10** | Suma resultados por máquina | `parse`, `part1`, `part2` |
| **Parser** | Regex + bitmasks + joltages | `parse(String)` → `List<Machine>` |
| **Machine** | VO: `lights`, `target`, `buttons`, `joltages` | record |

### Parte 1 — dominio

| Clase | Rol | API |
|-------|-----|-----|
| **ButtonSolver** | BFS sobre estados `0…2^n-1` | `minPresses(Machine)` |

Vecinos: `s XOR buttonMask`. Espacio ≤ 8192 nodos.

### Parte 2 — dominio + optimización

| Clase | Rol | API |
|-------|-----|-----|
| **JoltageSolver** | **Facade:** `Machine` → matrices → B&B | `minPresses(Machine)` |
| **Rational** | Fracciones exactas (`BigInteger`) | `of`, `add`, `sub`, `mul`, `div`, … |
| **SimplexSolver** | LP relajado dos fases | `solve(A, b, c, k, n)` → `Rational[]` |
| **BranchAndBoundSolver** | Enteriza con ramificación | `solve(A, b, c, k, n)` → `int` |

**Formulación ILP:** minimizar Σxⱼ s.a. Ax = b, x ≥ 0 entero; A[i][j] = bit i del botón j.

---

## 5. Modelo de clases UML

Diagrama de clases del módulo `aoc.dia10` (incluye subpaquete `optimization`). Notación UML 2.5 (misma convención que días 1–9):

- Visibilidad (`+`/`-`): **solo** dentro de cada caja; las flechas no llevan `+`/`-`.
- **`<<utility>>`**: sustituye repetir `{static}` en cada método.
- **Dependencia** (`..>`): creación o uso puntual con multiplicidad.
- No se incluyen `Day`, `Lines`, `List`, `Queue`, `BigInteger`, ni matrices internas (`Rational[][]`).

**Colección de máquinas.** `parse` devuelve `List<Machine>` en Java; se modela como dependencias `0..*` hacia `Machine`.

**`Machine`.** `lights` y `target` son primitivos en la caja; `buttons` y `joltages` son listas de `Integer` (JDK) — campos del record, sin clase contenedora en el diagrama.

**Parte 1 vs parte 2.** Mismas máquinas parseadas. Parte 1: `ButtonSolver` (BFS bitmask). Parte 2: `JoltageSolver` (`<<Facade>>`) adapta la máquina a ILP y delega en `BranchAndBoundSolver` → `SimplexSolver` → `Rational`.

```mermaid
classDiagram
    direction TB

    namespace aoc.dia10 {
        class Day10 {
            +number() int
            +parse(input String) List~Machine~
            +part1(machines List~Machine~) Object
            +part2(machines List~Machine~) Object
        }

        class Parser {
            <<utility>>
            +parse(input String) List~Machine~
        }
    }

    namespace aoc.dia10.model {
        class Machine {
            <<record>>
            +lights int
            +target int
            +buttons List~Integer~
            +joltages List~Integer~
        }

        class ButtonSolver {
            <<utility>>
            +minPresses(m Machine) int
        }

        class JoltageSolver {
            <<Facade>>
            +minPresses(m Machine) int
        }
    }

    namespace aoc.dia10.optimization {
        class Rational {
            +of(v long) Rational
            +add(o Rational) Rational
            +sub(o Rational) Rational
            +mul(o Rational) Rational
            +div(o Rational) Rational
        }

        class SimplexSolver {
            <<utility>>
            +solve(a Rational[][], b Rational[], c Rational[], k int, n int) Rational[]
        }

        class BranchAndBoundSolver {
            <<utility>>
            +solve(a Rational[][], b Rational[], c Rational[], k int, n int) int
        }
    }

    Day10 "1" ..> "1" Parser
    Day10 "1" ..> "0..*" Machine
    Day10 "1" ..> "1" ButtonSolver
    Day10 "1" ..> "1" JoltageSolver
    Parser "1" ..> "0..*" Machine
    ButtonSolver "1" ..> "1" Machine
    JoltageSolver "1" ..> "1" Machine
    JoltageSolver "1" ..> "1" BranchAndBoundSolver
    JoltageSolver "1" ..> "0..*" Rational
    BranchAndBoundSolver "1" ..> "1" SimplexSolver
    SimplexSolver "1" ..> "0..*" Rational
```

| Relación | Multiplicidad | Motivo en el código |
|----------|---------------|---------------------|
| `Day10` → `Parser` | `1` : `1` | `parse` delega en `Parser`. |
| `Day10` → `Machine` | `1` : `0..*` | Una línea de input → una máquina. |
| `Day10` → `ButtonSolver` | `1` : `1` | `part1` suma `minPresses` por máquina. |
| `Day10` → `JoltageSolver` | `1` : `1` | `part2` suma `minPresses` por máquina. |
| `Parser` → `Machine` | `1` : `0..*` | Regex + bitmasks + joltages por línea. |
| `ButtonSolver` → `Machine` | `1` : `1` | BFS sobre estados de una máquina. |
| `JoltageSolver` → `Machine` | `1` : `1` | `buildMatrix` lee botones y joltages. |
| `JoltageSolver` → `BranchAndBoundSolver` | `1` : `1` | Resuelve el ILP entero. |
| `JoltageSolver` → `Rational` | `1` : `0..*` | Construye `A`, `b`, `c` exactos. |
| `BranchAndBoundSolver` → `SimplexSolver` | `1` : `1` | Relajación LP en cada nodo B&B. |
| `SimplexSolver` → `Rational` | `1` : `0..*` | Tableau con aritmética exacta. |

**Detalle interno.** Helpers BFS, `buildMatrix`, fases del simplex y ramificación no aparecen en el diagrama. `ButtonSolver` y `optimization/*` no se mezclan.

---

## 6. Colaboración entre clases

```mermaid
flowchart TB
    subgraph p1 [Parte 1]
        BS[ButtonSolver]
        BS --> Machine
    end

    subgraph p2 [Parte 2]
        JS[JoltageSolver]
        JS -->|buildMatrix| Machine
        JS --> BnB[BranchAndBoundSolver]
        BnB --> Sx[SimplexSolver]
        Sx --> Rat[Rational]
    end
```

`ButtonSolver` y `optimization/*` **no se mezclan**: algoritmos distintos para reglas distintas del enunciado.

---

## 7. Decisiones de este día

| Decisión | Motivo |
|----------|--------|
| Extraer `optimization/` | ~260 líneas de simplex+B&B; frágil y reutilizable solo aquí |
| `JoltageSolver` como fachada en `model/` | Punto de entrada del dominio; oculta LP |
| `Rational` separado de simplex | Aritmética exacta vs tableau; testabilidad |
| `Machine.buttons` compartido | Misma lista: bitmask (p1) y columnas de A (p2) |
| No poner simplex en `aoc` global | Solo el día 10 necesita LP exacto |

---

## 8. Patrones

- **Facade:** `JoltageSolver`.
- **Adapter:** construcción de matrices desde `Machine`.
- **Value Object:** `Machine`, `Rational`.
- **Clase de utilidad:** solvers con constructor privado.

---

## 9. Dependencias compartidas

- `aoc.parse.Lines`
- `aoc.core.Day`

---

## 10. Notas de rendimiento

- Parte 2: ~500 ms/input real (B&B + simplex por máquina).
- Parseo único vía `Day<T>` evita repetir ese coste entre partes.
