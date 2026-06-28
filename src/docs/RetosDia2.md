# Día 2 — Gift Shop

> Documentación **arquitectónica** del módulo `aoc.dia2`.  
> Visión global: [ARQUITECTURA.md](./ARQUITECTURA.md).

---

## 1. Resumen del problema

- Entrada: rangos de IDs `a-b` separados por comas.
- **Parte 1:** IDs *inválidos* cuya representación decimal es la mitad repetida (ej. `6464`).
- **Parte 2:** IDs inválidos formados por un patrón repetido (ej. `111`, `121212`).
- Respuesta: suma de todos los IDs inválidos en todos los rangos.

---

## 2. Contrato del día

```java
public class Day02 implements Day<List<LongRange>>
```

| Parte | Modelo | Salida | Criterio (Strategy) |
|-------|--------|--------|---------------------|
| part1 | `List<LongRange>` | `long` | `InvalidIdChecker::isInvalid` |
| part2 | idem | `long` | `InvalidIdChecker::isInvalidExtended` |

---

## 3. Estructura de paquetes

```
aoc.dia2/
├── Day02.java
├── Parser.java
└── model/
    └── InvalidIdChecker.java
```

---

## 4. Catálogo de clases

| Clase | Rol | API principal | Depende de |
|-------|-----|---------------|------------|
| **Day02** | Orquestador; aplica distinta regla por parte | `parse`, `part1`, `part2` | `Parser`, `InvalidIdChecker` |
| **Parser** | Tokeniza rangos `a-b` | `parse(String)` | `LongRange.parse` |
| **InvalidIdChecker** | Reglas de validez + barrido de rango | `isInvalid`, `isInvalidExtended`, `findInvalidIdsIn(range, predicate)` | `LongRange` |

---

## 5. Colaboración entre clases

```
Day02.partN(ranges)
  └─ sumInvalidIds(ranges, LongPredicate)
       └─ por cada LongRange:
            InvalidIdChecker.findInvalidIdsIn(range, predicate)
                 └─ for id in [start..end]: if predicate.test(id) → acumular
```

`Day02` no implementa las reglas: solo elige la **estrategia** (`LongPredicate`) y suma.

---

## 6. Decisiones de este día

| Decisión | Motivo |
|----------|--------|
| Eliminar `IdRange` local → `aoc.parse.LongRange` | Mismo concepto que día 5; DRY transversal |
| `LongPredicate` como parámetro | Partes 1 y 2 comparten el bucle; solo cambia la regla |
| Lógica de patrones en `InvalidIdChecker` estático | Sin estado; funciones puras sobre `long` |

---

## 7. Patrones

- **Strategy:** `LongPredicate` intercambiable entre partes (method references).
- **Value Object:** `LongRange` compartido (`contains`, `length`).
- **Template Method:** flujo común en `sumInvalidIds`.

---

## 8. Dependencias compartidas

- `aoc.parse.LongRange` — parseo y rango inclusivo
- `aoc.core.Day`
