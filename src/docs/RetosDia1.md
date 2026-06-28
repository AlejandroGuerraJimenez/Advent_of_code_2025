# Día 1 — Secret Entrance

> Documentación **arquitectónica** del módulo `aoc.dia1`.  
> Visión global: [ARQUITECTURA.md](./ARQUITECTURA.md).

---

## 1. Resumen del problema

- Dial circular 0–99, posición inicial 50.
- Entrada: rotaciones `L`/`R` + distancia por línea.
- **Parte 1:** veces que el dial queda en `0` al **terminar** cada rotación.
- **Parte 2:** veces que pasa por `0` en **cualquier clic** de la rotación.

---

## 2. Contrato del día

```java
public class Day01 implements Day<List<Rotation>>
```

| Parte | Entrada (modelo) | Salida | Delegación |
|-------|------------------|--------|------------|
| parse | `String` | `List<Rotation>` | `Parser` |
| part1 | `List<Rotation>` | `int` | `Dial.rotate` + `isZero` |
| part2 | `List<Rotation>` | `int` | `Dial.rotate` (cuenta clics en 0) |

El modelo se parsea **una vez**; ambas partes reutilizan la misma lista.

---

## 3. Estructura de paquetes

```
aoc.dia1/
├── Day01.java
├── Parser.java
└── model/
    ├── Dial.java
    ├── Rotation.java      record
    └── Direction.java       enum
```

---

## 4. Catálogo de clases

| Clase | Rol | API principal | Depende de |
|-------|-----|---------------|------------|
| **Day01** | Orquestador; implementa `Day<List<Rotation>>` | `parse`, `part1`, `part2` | `Parser`, `Dial` |
| **Parser** | Adapta líneas `L68` → `Rotation` | `parse(String)` | `Lines`, `Direction` |
| **Dial** | Estado del dial + reglas de giro | `rotate(Rotation)`, `isZero()` | `Rotation`, aritmética modular |
| **Rotation** | VO inmutable: dirección + pasos | record | `Direction` |
| **Direction** | `LEFT` / `RIGHT` | enum | — |

---

## 5. Colaboración entre clases

```mermaid
sequenceDiagram
    participant DR as DayRunner
    participant D01 as Day01
    participant P as Parser
    participant Dial as Dial

    DR->>D01: parse(input)
    D01->>P: parse(input)
    P-->>D01: List Rotation
    DR->>D01: part1(model)
    loop cada Rotation
        D01->>Dial: rotate(r)
        D01->>Dial: isZero()
    end
```

**Parte 1 vs 2:** misma secuencia de rotaciones; `Dial.rotate` en parte 2 devuelve cuántos clics pasaron por 0 (simulación clic a clic internamente).

---

## 6. Decisiones de este día

| Decisión | Motivo |
|----------|--------|
| Estado mutable en `Dial`, datos inmutables en `Rotation` | El dial evoluciona; cada línea del input es un valor fijo |
| Lógica del dial en `model/`, no en `Day01` | SRP: el día solo orquesta; las reglas del puzzle viven en el dominio |
| `Parser` usa `aoc.parse.Lines` | Reutilizar filtrado de líneas en blanco (transversal) |

---

## 7. Patrones

- **Template Method:** `Day01` rellena los hooks del contrato `Day<T>`.
- **Value Object:** `Rotation` (record), `Direction` (enum).
- **Rich domain model:** `Dial` encapsula posición y wrap-around.

---

## 8. Dependencias compartidas

- `aoc.core.Day`
- `aoc.parse.Lines` (en `Parser`)
