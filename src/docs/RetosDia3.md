# Día 3 — Lobby

> Documentación **arquitectónica** del módulo `aoc.dia3`.  
> Visión global: [ARQUITECTURA.md](./ARQUITECTURA.md).

---

## 1. Resumen del problema

- Cada línea es un banco de baterías (dígitos).
- Hay que elegir **k** dígitos en orden (greedy por máximo joltage) y formar un número.
- **Parte 1:** k = 2. **Parte 2:** k = 12.
- Respuesta: suma del joltage máximo de cada banco.

---

## 2. Contrato del día

```java
public class Day03 implements Day<List<BatteryBank>>
```

| Constante | Valor | Uso |
|-----------|-------|-----|
| `PART1_BATTERIES` | 2 | part1 |
| `PART2_BATTERIES` | 12 | part2 |

Ambas partes llaman a `sumMaxJoltage(banks, k)` con distinto `k`.

---

## 3. Estructura de paquetes

```
aoc.dia3/
├── Day03.java
├── Parser.java
└── model/
    ├── BatteryBank.java    record(digits)
    └── JoltageCalculator.java
```

---

## 4. Catálogo de clases

| Clase | Rol | API principal | Depende de |
|-------|-----|---------------|------------|
| **Day03** | Orquestador; parametriza k por parte | `parse`, `part1`, `part2` | `Parser`, `JoltageCalculator` |
| **Parser** | Una línea → un `BatteryBank` | `parse(String)` | `Lines` |
| **BatteryBank** | VO: cadena de dígitos del banco | `digits()` | — |
| **JoltageCalculator** | Selección greedy de k dígitos | `maxJoltage(bank, count)` | `BatteryBank` |

---

## 5. Colaboración entre clases

```
Parser → List<BatteryBank>
Day03 → banks.stream().mapToLong(b → JoltageCalculator.maxJoltage(b, k)).sum()
JoltageCalculator → selectDigits (greedy) → Long.parseLong
```

La variación entre partes está **solo en `Day03`** (constantes nombradas); el algoritmo es el mismo.

---

## 6. Decisiones de este día

| Decisión | Motivo |
|----------|--------|
| Magic numbers → `PART1_BATTERIES` / `PART2_BATTERIES` | Documentar en el orquestador la diferencia entre partes |
| Algoritmo en clase dedicada, no en `Day03` | SRP; el día no conoce la greedy |
| `BatteryBank` como record mínimo | Separar “línea parseada” de la lógica numérica |

---

## 7. Patrones

- **Value Object:** `BatteryBank`.
- **Parametrización por constante:** evita duplicar `part1`/`part2` completos.

---

## 8. Dependencias compartidas

- `aoc.parse.Lines`
- `aoc.core.Day`
