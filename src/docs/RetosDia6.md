# Reto Día 6 — Trash Compactor

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 6 de Advent of Code 2025.

---

## 1. El problema

La hoja de ejercicios matemáticos está dispuesta como un **grid horizontal**: los
problemas están uno al lado del otro y separados por **columnas de espacios
completos** (todas las filas tienen un espacio en esa columna). Dentro de cada
problema, los números se leen verticalmente y el operador (`+` o `*`) aparece en
la última fila.

### Parte 1 — Lectura horizontal (cada fila es un número)

Cada problema ocupa un grupo de columnas contiguas. Los números de ese problema
son las filas superiores (excluyendo la última) leídas de izquierda a derecha y
con `trim()`. Se aplica el operador de la última fila a todos los números del
problema. El resultado es la **suma de todos los resultados**.

Ejemplo:
```
123 328  51 64
 45 64  387 23
  6 98  215 314
*   +   *   +
```
→ `33210 + 490 + 4243455 + 401 = 4277556`

### Parte 2 — Lectura vertical (cada columna es un número, de derecha a izquierda)

En la lectura cefálópoda, **cada columna** dentro de un problema representa un
número: sus dígitos son los caracteres de esa columna de arriba a abajo
(más significativo primero), ignorando espacios. Los problemas se procesan de
derecha a izquierda.

Ejemplo (mismo grid):
- Problema derecho (cols 12-14): `4`, `431`, `623` con `+` → `1058`
- Total → `1058 + 3253600 + 625 + 8544 = 3263827`

---

## 2. Estructura de paquetes

```
aoc.dia6
├── Day06.java            ← Entry point del día
├── Parser.java           ← Parseo del grid horizontal
└── model/
    ├── Problem.java       ← Value object: números + operador
    ├── MathWorksheet.java ← Agrupación de todos los problemas
    └── WorksheetSolver.java ← Evaluación y suma total
```

---

## 3. Flujo de ejecución

### Parte 1
```
input (String)
  → Parser.parse()          detección de grupos de columnas
  → MathWorksheet           lista de Problem (fila→número)
  → WorksheetSolver.grandTotal()
  → long
```

### Parte 2
```
input (String)
  → Parser.parseVertical()  misma detección de grupos
  → MathWorksheet           lista de Problem (columna→número, RTL)
  → WorksheetSolver.grandTotal()  mismo evaluador
  → long
```

---

## 4. Explicación clase a clase

### `Problem` — record
```java
public record Problem(List<Long> numbers, char operator) {}
```
Value object inmutable que encapsula un problema matemático. Usa `long` para
los números porque el producto de varios números grandes puede superar
`Integer.MAX_VALUE`. El operador es un `char` (`'+'` o `'*'`).

### `MathWorksheet` — record
```java
public record MathWorksheet(List<Problem> problems) {}
```
Agregación de todos los problemas parseados. Es un contenedor semántico que
hace explícito el dominio: "esto es una hoja de ejercicios matemáticos",
no simplemente una lista.

### `WorksheetSolver` — servicio de dominio
```java
public static long grandTotal(MathWorksheet ws) {
    return ws.problems().stream().mapToLong(WorksheetSolver::solve).sum();
}
private static long solve(Problem p) {
    var nums = p.numbers().stream().mapToLong(Long::longValue);
    return p.operator() == '*' ? nums.reduce(1L, (a, b) -> a * b) : nums.sum();
}
```
Aplica la operación correspondiente a cada problema usando la API de streams.
Para `*` usa `reduce(1L, ...)` (elemento neutro de la multiplicación), para
`+` usa `sum()`. No tiene estado — es un servicio funcional puro.

### `Parser` — el más complejo del día
El parsing es la parte más técnicamente interesante del día 6. Requiere leer un
grid 2D con estructura implícita de columnas.

**Detección de columnas separadoras:**
```java
private static boolean isSeparatorColumn(List<String> rows, int col, int width) {
    if (col >= width) return true;
    return rows.stream().allMatch(r -> col >= r.length() || r.charAt(col) == ' ');
}
```
Una columna es separadora si **todos** los renglones tienen un espacio en esa
posición (o el renglón es más corto). Esto localiza los límites entre problemas.

**Agrupación de columnas:**
El algoritmo recorre todas las posiciones de columna de izquierda a derecha,
acumulando el inicio de cada grupo y cerrándolo al encontrar un separador.

**Parseo horizontal (Parte 1):**
```java
private static Problem parseProblem(List<String> rows, int[] group) {
    char op = slice(rows.getLast(), group).trim().charAt(0);
    List<Long> numbers = rows.subList(0, rows.size() - 1).stream()
            .map(r -> slice(r, group).trim())
            .filter(s -> !s.isEmpty())
            .map(Long::parseLong)
            .toList();
    return new Problem(numbers, op);
}
```
Se extrae el slice de columnas para cada fila y se hace `trim()` para ignorar
el alineamiento izquierdo/derecho del enunciado.

**Parseo vertical (Parte 2):**
```java
private static List<Long> buildNumbersRtl(List<String> numberRows, int[] group) {
    List<Long> numbers = new ArrayList<>();
    for (int col = group[1]; col >= group[0]; col--) {
        String digits = columnDigits(numberRows, col);
        if (!digits.isEmpty()) numbers.add(Long.parseLong(digits));
    }
    return numbers;
}
```
Se recorre el grupo de derecha a izquierda. Para cada columna se concatenan
los dígitos de todas las filas (ignorando espacios) para formar el número.

---

## 5. Principios de diseño aplicados

| Principio | Aplicación |
|-----------|-----------|
| **SRP** | `Parser` solo parsea, `WorksheetSolver` solo evalúa, `Problem` solo agrupa datos |
| **OCP** | `WorksheetSolver.grandTotal()` funciona igual para parte 1 y parte 2; solo cambia el parser |
| **Inmutabilidad** | `Problem` y `MathWorksheet` son `record` (inmutables por naturaleza) |
| **DRY** | `WorksheetSolver` se reutiliza intacto en ambas partes — no hay duplicación de lógica de evaluación |
| **Separación de concerns** | La lógica de parsing vertical/horizontal está totalmente aislada del evaluador |

---

## 6. Decisiones técnicas

### ¿Por qué detectar separadores por columna completa?
El enunciado dice: "Problems are separated by a full column of only spaces."
La detección revisa **todas** las filas en esa columna, lo que es robusto ante
cualquier alineamiento de los números (izquierda, derecha, o centrado).

### ¿Por qué `long` para los números?
En la Parte 2, los números pueden tener hasta tantos dígitos como filas tiene
el grid. Con filas de 4 números y el producto entre ellos, `int` desbordaría.
`long` soporta hasta `9.2 × 10^18`.

### ¿Por qué no filtrar líneas en blanco en Parte 1?
Las líneas de la hoja de ejercicios nunca son en blanco — tienen puntos o
números. El `filter(l -> !l.isBlank())` limpia únicamente posibles líneas
vacías al inicio/final del archivo. (Nota: esto fue corregido en Día 7 donde
las líneas vacías sí son parte del grid.)

### ¿Por qué `slice()` con `Math.min`?
Protege contra filas de longitud variable: si una fila es más corta que el
rango `[group[0], group[1]]`, se devuelve el substring disponible o vacío.

---

## 7. Resultados

| Parte | Respuesta |
|-------|-----------|
| Parte 1 | `6503327062445` |
| Parte 2 | `9640641878593` |

---

## 8. Defensa del desarrollo

**P: ¿Por qué el parser detecta grupos por columna entera en lugar de parsear
por posición del operador en la última fila?**

R: Detectar por el operador sería más frágil: si el operador fuera un dígito o
estuviera alineado de forma inusual, fallaría. Detectar columnas de espacios es
la definición exacta del enunciado ("a full column of only spaces") y funciona
independientemente del contenido.

**P: ¿No sería más simple dividir por espacios múltiples?**

R: No, porque dentro de un problema los números también pueden estar separados
por espacios (por el alineamiento). Un `split("  +")` rompería los números
internos. La detección columna a columna es la única solución robusta.

**P: ¿Por qué `WorksheetSolver` es una clase de utilidad y no forma parte de
`MathWorksheet`?**

R: Porque `MathWorksheet` es un value object de dominio (agrupa datos), mientras
que `WorksheetSolver` es un servicio de dominio (realiza cómputos). Mezclarlos
violaría el SRP: si el modo de evaluación cambia (p. ej., nueva operación `-`),
no deberíamos tocar el value object.

**P: ¿Por qué la misma clase `WorksheetSolver` funciona para ambas partes?**

R: Porque el cambio entre parte 1 y parte 2 es **solo de representación**
(cómo se codifican los números en el grid), no de operación matemática. La
evaluación `sum` / `product` es idéntica. Esto es el OCP en acción: se extiende
el comportamiento añadiendo un nuevo parser sin modificar el evaluador.

---

## Mejoras arquitectónicas aplicadas

### Fase 1 — Core: interfaz `Day<T>` con parseo único
- **Nuevo record `Worksheets(horizontal, vertical)`** en `model/`: encapsula las
  dos lecturas del mismo input. `Day06.parse` construye **ambas una sola vez**
  (`Parser.parse` + `Parser.parseVertical`); antes cada parte parseaba por su
  cuenta dentro de `part1`/`part2`.
- `Day06` implementa `Day<Worksheets>`; `part1` usa la horizontal y `part2` la
  vertical.
- `part1`/`part2` devuelven `Object`; el `toString` lo hace `DayRunner`.
- Se añadió `number()`. La salida muestra la etiqueta y el resultado de cada parte.

### Fase 2 — Utilidades de parseo (`aoc.parse`)
- `Parser.parse` y `Parser.parseVertical` usan `Lines.nonBlank(input)`.
