# Reto Día 3 — Printing Department

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 3 de Advent of Code 2025.

---

## 1. El problema

El escalator del departamento de impresión necesita energía. Hay baterías
dispuestas en bancos; cada línea del input es un banco representado como una
cadena de dígitos del 1 al 9. Encender una batería contribuye con su dígito al
número de salida del banco.

### Parte 1 — Seleccionar exactamente 2 baterías

De cada banco hay que elegir exactamente **2 baterías** (sin reordenarlas) para
formar el número de 2 dígitos más grande posible. El resultado es la suma de
todos los máximos.

Ejemplo:
- `987654321111111` → `98` (las dos primeras)
- `811111111111119` → `89` (la 8 al inicio, la 9 al final)
- `234234234234278` → `78` (7 y 8 al final)
- `818181911112111` → `92` (el 9 en pos 6, el 2 en pos 11)
- Total: `98 + 89 + 78 + 92 = 357`

### Parte 2 — Seleccionar exactamente 12 baterías

La misma regla, pero ahora se eligen **12 baterías** formando el número de 12
dígitos más grande. El resultado sigue siendo la suma de todos los máximos.

---

## 2. Estructura del paquete

```
dia3/
├── Day03.java              → implements Day; orquesta los dos retos
├── Parser.java             → una línea = un BatteryBank
└── model/
    ├── BatteryBank.java    → record(String digits)
    └── JoltageCalculator.java → algoritmo greedy de selección de dígitos
```

---

## 3. Flujo de ejecución

```
Day03.part1 / part2
  └─ Parser.parse(input)
        └─ una línea → BatteryBank::new
  └─ sumMaxJoltage(banks, count=2 ó 12)
        └─ JoltageCalculator.maxJoltage(bank, count)
              └─ selectDigits(digits, count)
                    └─ por cada posición k:
                         appendBestDigit → maxDigitPos(digits, from, to)
        └─ suma todos los máximos → String
```

---

## 4. Explicación clase por clase

### `Day03` (paquete `aoc.dia3`)

Implementa `Day`. Contiene un método privado `sumMaxJoltage(banks, count)` que
elimina la duplicación entre ambos retos: la única diferencia entre parte 1 y
parte 2 es el número de baterías a encender.

```java
// Parte 1: máximo número de 2 dígitos por banco
return sumMaxJoltage(Parser.parse(input), 2);

// Parte 2: máximo número de 12 dígitos por banco
return sumMaxJoltage(Parser.parse(input), 12);
```

### `Parser` (paquete `aoc.dia3`)

Divide el input línea a línea y construye un `BatteryBank` por cada línea no
vacía, usando una referencia a constructor (`BatteryBank::new`).

### `BatteryBank` (paquete `aoc.dia3.model`)

`record` inmutable que encapsula una línea de dígitos. Representa un banco de
baterías tal como lo describe el enunciado. Al ser un record, tiene `equals`,
`hashCode` y `toString` gratuitos.

```java
public record BatteryBank(String digits) {}
```

### `JoltageCalculator` (paquete `aoc.dia3.model`)

Contiene toda la lógica de selección óptima de dígitos. Tiene cuatro métodos,
cada uno con una responsabilidad concreta y ≤ 5 líneas:

**`maxJoltage(BatteryBank, int count)`** — Punto de entrada público. Delega en
`selectDigits` y convierte el resultado a `long`.

**`selectDigits(String digits, int count)`** — Construye el número óptimo
carácter a carácter. Para cada posición k llama a `appendBestDigit` y avanza
el puntero de inicio al dígito elegido + 1.

**`appendBestDigit(StringBuilder, String, int from, int to)`** — Encuentra el
mejor dígito en el rango [from, to], lo añade al resultado y devuelve su posición.

**`maxDigitPos(String digits, int from, int to)`** — Devuelve el índice del
dígito de mayor valor entre las posiciones `from` y `to` (inclusive).

---

## 5. El algoritmo: greedy con ventana deslizante

El problema central es: elegir `count` índices `i₁ < i₂ < … < iₙ` de una cadena
de dígitos para maximizar el número `d[i₁]d[i₂]…d[iₙ]`.

La solución es **greedy**:

> Para elegir el dígito en la posición k (de izquierda a derecha del resultado),
> toma el máximo del rango `[start, n - (count - k)]`, donde el límite derecho
> garantiza que queden suficientes dígitos para completar las posiciones restantes.

El límite derecho es la clave: `n - (count - k)` asegura que, tras elegir en
esta posición, aún quedan `count - k - 1` posiciones por llenar con dígitos que
están a la derecha del elegido.

**Demostración del greedy:** el dígito más a la izquierda del resultado tiene
peso `10^(count-1)`, mucho mayor que todos los siguientes. Por tanto, maximizarlo
primero es siempre óptimo. Si hay empate en valor, elegir la posición más a la
izquierda (el `maxDigitPos` devuelve el primero encontrado) deja más opciones
para los dígitos siguientes, lo que nunca empeora el resultado.

Verificación con el ejemplo de la parte 1 (`count=2`):

| Banco               | Rango primera pos.  | Max | Pos | Rango segunda pos. | Max | Resultado |
|---------------------|---------------------|-----|-----|--------------------|-----|-----------|
| `987654321111111`   | `[0..13]`           | 9   | 0   | `[1..14]`          | 8   | `98` ✓    |
| `811111111111119`   | `[0..13]`           | 8   | 0   | `[1..14]`          | 9   | `89` ✓    |
| `234234234234278`   | `[0..13]`           | 7   | 13  | `[14..14]`         | 8   | `78` ✓    |
| `818181911112111`   | `[0..13]`           | 9   | 6   | `[7..14]`          | 2   | `92` ✓    |

---

## 6. Principios de diseño aplicados

### SRP — Responsabilidad única

| Clase | Su único motivo de cambio |
|---|---|
| `Parser` | Cambia el formato del input (líneas de dígitos) |
| `BatteryBank` | Cambia la estructura de un banco |
| `JoltageCalculator` | Cambia el algoritmo de selección óptima |
| `Day03` | Cambia cómo se combinan las piezas para cada reto |

### DRY — No te repitas

La diferencia entre parte 1 y parte 2 es un único entero (`count`). Ambos retos
comparten el mismo `sumMaxJoltage` y el mismo `JoltageCalculator.maxJoltage`.
No hay ninguna línea de lógica duplicada.

### OCP — Abierto/Cerrado

El `JoltageCalculator` soporta cualquier `count` sin modificación. Añadir una
hipotética Parte 3 con `count=20` solo requeriría una línea en `Day03`.

### Métodos cortos (≤ 5 líneas de cuerpo)

`JoltageCalculator` está dividido en cuatro métodos con responsabilidades
distintas e independientes. Cada método es legible y testeable de forma aislada:

```5:28:src/main/java/aoc/dia3/model/JoltageCalculator.java
    public static long maxJoltage(BatteryBank bank, int count) {
        return Long.parseLong(selectDigits(bank.digits(), count));
    }

    private static String selectDigits(String digits, int count) {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        for (int k = 0; k < count; k++)
            start = appendBestDigit(sb, digits, start, digits.length() - (count - k)) + 1;
        return sb.toString();
    }

    private static int appendBestDigit(StringBuilder sb, String digits, int from, int to) {
        int pos = maxDigitPos(digits, from, to);
        sb.append(digits.charAt(pos));
        return pos;
    }

    private static int maxDigitPos(String digits, int from, int to) {
        int best = from;
        for (int i = from + 1; i <= to; i++)
            if (digits.charAt(i) > digits.charAt(best)) best = i;
        return best;
    }
```

### Value Object / Record

`BatteryBank` es un record inmutable. Representa exactamente el concepto del
enunciado: un banco de baterías con sus dígitos. Sin estado mutable, sin setters.

---

## 7. Resultados verificados

| Entrada | Parte 1 | Parte 2 |
|---|---|---|
| Ejemplo del enunciado (4 bancos) | 357 | 3121910778619 |
| Input real (200 bancos) | 16927 | 167384358365132 |

---

## 8. Defensa del desarrollo

**¿Por qué el algoritmo greedy es correcto y no necesitas fuerza bruta?**
Porque el número de `count` dígitos tiene sus dígitos ponderados de mayor a
menor de izquierda a derecha. El dígito de la izquierda siempre vale más que la
suma de todos los que le siguen (ya que `9 * 10^(k-1) > 9 * (10^(k-1) - 1) / 9`).
Por tanto, maximizarlo primero es siempre la decisión óptima, y la elección de
cada dígito subsiguiente es independiente de las anteriores dado el rango
disponible.

**¿Por qué `count` como parámetro y no dos métodos separados?**
Porque la lógica es idéntica; solo varía un número. Tener dos métodos implicaría
duplicar código o uno delegando en el otro de forma artificial. Un parámetro
expresa directamente que "parte 1 elige 2" y "parte 2 elige 12" son el mismo
algoritmo con diferente configuración.

**¿Por qué `long` en el resultado y no `int`?**
Porque 12 dígitos de máximo valor son `999999999999` (~10¹²), que desborda
`int`. La suma de 200 bancos con ese valor sería ~2×10¹⁴, también fuera del
rango de `int`. `long` cubre hasta ~9.2×10¹⁸.

**¿Cómo testearías `maxDigitPos`?**
Con casos directos: todos iguales (devuelve 0), máximo al final, máximo al
principio, empate (debe devolver el primero). Es una función pura sin efectos
secundarios, trivialmente testeable.

**¿Qué pasa si un banco tiene menos dígitos que `count`?**
El algoritmo lanzaría `StringIndexOutOfBoundsException` porque el rango
`[from, n-(count-k)]` se volvería negativo. El enunciado garantiza que todos los
bancos tienen al menos `count` dígitos, así que no se valida. En un sistema de
producción añadiría una precondición en `BatteryBank` o en `JoltageCalculator`.
