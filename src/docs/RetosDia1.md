# Reto Día 1 — Secret Entrance

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 1 de Advent of Code 2025.

## 1. El problema

Una caja fuerte tiene un dial circular con números del `0` al `99`. El dial
empieza apuntando al `50`. La entrada es una secuencia de rotaciones, una por
línea, con formato `L`/`R` (izquierda/derecha) seguido de una distancia en clics.

- **Parte 1:** contar cuántas veces el dial queda apuntando al `0` **al terminar**
  cada rotación.
- **Parte 2** (`método 0x434C49434B` = "CLICK"): contar cuántas veces el dial
  pasa por el `0` en **cualquier clic**, ya sea durante o al final de una rotación.

## 2. Visión general de la arquitectura

El proyecto separa la **infraestructura común** (válida para los 25 días) de la
**lógica concreta de cada día**.

```
aoc
├── Main                 -> Punto de entrada. Monta y arranca la aplicación.
├── core                 -> Infraestructura reutilizable por todos los días.
│   ├── Day              -> Contrato (interfaz) que cumple cada día.
│   ├── DayRunner        -> Orquesta la ejecución de un día.
│   └── InputReader      -> Lee el fichero de entrada desde el classpath.
├── registry
│   └── DayRegistry      -> Mapa día -> implementación.
└── dia1                 -> Solución concreta del Día 1.
    ├── Day01            -> Resuelve part1 y part2 (los dos retos del día).
    ├── Parser           -> Convierte el texto en objetos de dominio.
    └── Model
        ├── Dial         -> Modelo del dial (estado + comportamiento).
        ├── Rotation     -> Dato inmutable: dirección + pasos.
        └── Direction    -> Enum LEFT / RIGHT.
```

### Flujo de ejecución

```
Main
  └─ construye DayRegistry e InputReader
  └─ DayRunner.run(dia)
        ├─ InputReader.read(dia)      -> String de entrada
        ├─ DayRegistry.get(dia)       -> Day correspondiente
        └─ day.part1(input) / day.part2(input)
              └─ Parser.parse(input)  -> List<Rotation>
              └─ Dial.apply / countZerosDuring
```

## 3. Capas y responsabilidades

| Capa | Clase | Responsabilidad única |
|------|-------|-----------------------|
| Arranque | `Main` | Componer las piezas e iniciar la ejecución. |
| Orquestación | `DayRunner` | Coordinar lectura + resolución + impresión. |
| Acceso a datos | `InputReader` | Obtener el texto de entrada. |
| Registro | `DayRegistry` | Resolver qué `Day` corresponde a un número. |
| Contrato | `Day` | Definir la forma de toda solución (`part1`, `part2`). |
| Caso de uso | `Day01` | Resolver los dos retos del día apoyándose en el dominio. |
| Parsing | `Parser` | Traducir texto a objetos de dominio. |
| Dominio | `Dial`, `Rotation`, `Direction` | Modelar las reglas del dial. |

## 4. Explicación clase por clase

A continuación se describe qué hace cada clase, qué responsabilidad tiene y por
qué está donde está.

### `Main` (paquete `aoc`)

Es el **punto de entrada** de la aplicación. Su único trabajo es *componer* el
sistema (montar las piezas) y arrancarlo; no contiene lógica del reto.

- Lee el número de día de los argumentos del programa (con `1` por defecto si no
  se pasa ninguno).
- Construye el `DayRegistry` (el mapa de días disponibles) y el `InputReader`.
- Crea el `DayRunner` inyectándole esas dependencias y lo ejecuta.

Es lo que en arquitectura se llama el *Composition Root*: el único sitio donde se
hacen los `new` de las piezas principales y se cablean entre sí.

### `Day` (paquete `aoc.core`)

Es una **interfaz**: el contrato que toda solución diaria debe cumplir. Obliga a
cada día a ofrecer dos métodos, `part1(input)` y `part2(input)`, que devuelven la
respuesta como `String`. Gracias a esta abstracción el resto del sistema puede
tratar todos los días por igual sin conocer su implementación concreta.

### `DayRunner` (paquete `aoc.core`)

Es el **orquestador** de la ejecución de un día. Recibe por constructor un
`DayRegistry` y un `InputReader` (inyección de dependencias). Su método `run`:

1. Pide al `InputReader` el texto de entrada del día.
2. Pide al `DayRegistry` la implementación `Day` correspondiente.
3. Imprime el resultado de `part1` y `part2`.

No sabe *cómo* se lee la entrada ni *cómo* se resuelve el reto: solo coordina.

### `InputReader` (paquete `aoc.core`)

Es la capa de **acceso a datos**. Su responsabilidad es obtener el texto de la
entrada de un día. Lo lee desde el *classpath* como recurso
(`/InputNN.txt`), de modo que funciona igual al ejecutar desde el IDE o desde un
JAR, sin depender del directorio de trabajo. Si el recurso no existe lanza una
excepción descriptiva.

### `DayRegistry` (paquete `aoc.registry`)

Es un **registro** (un envoltorio sobre un `Map<Integer, Day>`). Traduce un número
de día a su implementación `Day`. Centraliza en un solo sitio qué días están
disponibles, de modo que añadir un día nuevo solo afecta a este mapa.

### `Day01` (paquete `aoc.dia1`)

Es la **solución concreta del Día 1**; implementa la interfaz `Day`. Actúa como
*caso de uso*: orquesta el dominio para responder a los dos retos.

- `part1`: parsea la entrada, aplica cada rotación al `Dial` y cuenta cuántas
  veces queda en `0` al terminar.
- `part2`: parsea la entrada y suma, por cada rotación, cuántas veces el dial
  pasa por `0` clic a clic.

No manipula la posición del dial a mano: delega todo en `Dial`.

### `Parser` (paquete `aoc.dia1`)

Es el **traductor** de texto a objetos de dominio. Convierte cada línea (`"L68"`)
en un `Rotation` (dirección + pasos), descartando líneas en blanco. Aísla el
formato del fichero del resto del código: si cambiara el formato de entrada, solo
cambiaría esta clase.

### `Dial` (paquete `aoc.dia1.Model`)

Es el **corazón del dominio**: modela el dial físico. Guarda su estado
(`position`, que empieza en `50` y es privado) y expone su comportamiento:

- `apply(Rotation)`: gira el dial directamente al resultado final de la rotación
  (un salto), aplicando aritmética modular para el wrap-around circular.
- `countZerosDuring(Rotation)`: simula la rotación **clic a clic** y cuenta cada
  vez que pasa por `0` (necesario para la Parte 2 y casos con varias vueltas).
- `click(Direction)`: método privado que avanza un solo clic.
- `isZero()`: indica si el dial apunta a `0`.

Concentra todas las reglas del dial en un único lugar (modelo de dominio rico).

### `Rotation` (paquete `aoc.dia1.Model`)

Es un **`record`** (dato inmutable) que representa una rotación: una `Direction` y
un número de `steps`. Al ser inmutable, no puede modificarse tras crearse, lo que
evita errores de estado compartido.

### `Direction` (paquete `aoc.dia1.Model`)

Es un **`enum`** con dos valores: `LEFT` y `RIGHT`. Sustituye los caracteres
mágicos `'L'`/`'R'` por valores con nombre y seguridad de tipos, haciendo el
código más legible y evitando comparaciones de caracteres por todo el proyecto.

## 5. Principios de diseño aplicados

### SRP — Responsabilidad única (Single Responsibility Principle)

Cada clase tiene un único motivo para cambiar:

- `Parser` solo cambia si cambia el formato del texto.
- `Dial` solo cambia si cambian las reglas del dial.
- `InputReader` solo cambia si cambia el origen de los datos.
- `Day01` solo cambia si cambia la forma de combinar el dominio para resolver el reto.

La lectura, el parseo, el modelado y la orquestación están separados en clases
distintas en lugar de mezclarse en un único `main`.

### OCP — Abierto/Cerrado (Open/Closed Principle)

Añadir el Día 2 **no obliga a modificar** el código existente: basta con crear
`Day02 implements Day` y registrarlo en el `Map` del `DayRegistry`. `DayRunner`,
`InputReader` y el resto de la infraestructura permanecen intactos.

### DIP — Inversión de dependencias (Dependency Inversion Principle)

`DayRunner` depende de la **abstracción** `Day`, no de `Day01` concreto. Trabaja
contra el contrato, así que puede ejecutar cualquier día sin conocer su
implementación.

```16:20:src/main/java/aoc/core/DayRunner.java
        String input = inputReader.read(dayNumber);
        Day day = registry.get(dayNumber);

        System.out.println(day.part1(input));
        System.out.println(day.part2(input));
```

### Inyección de dependencias (Dependency Injection)

`DayRunner` no crea sus colaboradores: los recibe por constructor. Esto
desacopla la creación del uso y facilita las pruebas (se le puede pasar un
`InputReader` o `DayRegistry` falso).

```10:13:src/main/java/aoc/core/DayRunner.java
    public DayRunner(DayRegistry registry, InputReader inputReader){
        this.registry = registry;
        this.inputReader = inputReader;
    }
```

### Programar contra interfaces

El contrato `Day` define la forma de toda solución. El resto del sistema habla
con esa interfaz, no con clases concretas.

```3:6:src/main/java/aoc/core/Day.java
public interface Day {
    String part1(String input);
    String part2(String input);
}
```

### Modelo de dominio rico (Rich Domain Model)

La lógica del dial vive **dentro** de `Dial`, no esparcida por `Day01`. El dial
conoce su propio estado (`position`) y cómo se comporta (`apply`,
`countZerosDuring`, `isZero`). `Day01` solo orquesta; no manipula la posición a mano.

```7:33:src/main/java/aoc/dia1/Model/Dial.java
    public void apply(Rotation r) {
        if (r.direction() == Direction.RIGHT) {
            position = (position + r.steps()) % 100;
        } else {
            position = ((position - r.steps()) % 100 + 100) % 100;
        }
    }

    public int countZerosDuring(Rotation r) {
        int count = 0;
        for (int step = 0; step < r.steps(); step++) {
            click(r.direction());
            if (isZero()) {
                count++;
            }
        }
        return count;
    }
```

### Encapsulación

`position` es `private`. El exterior no puede corromper el estado del dial; solo
puede interactuar a través de métodos que garantizan las reglas (el módulo `% 100`
y el wrap-around circular).

### Inmutabilidad y tipos expresivos

- `Rotation` es un `record`: dato inmutable que representa una rotación.
- `Direction` es un `enum`: sustituye caracteres mágicos (`'L'`/`'R'`) por
  valores con significado y seguridad de tipos.

```3:3:src/main/java/aoc/dia1/Model/Rotation.java
public record Rotation(Direction direction, int steps) {}
```

### Separación parsing / cálculo

El texto se traduce a objetos (`Parser`) **antes** de aplicar la lógica. El
dominio (`Dial`) nunca toca `String`; trabaja con `Rotation`. Esto mantiene el
cálculo limpio y testeable de forma aislada.

## 6. Cómo se resuelve cada reto en `Day01`

Ambas partes comparten el mismo modelo (`Dial`) y solo cambian la pregunta:

```12:24:src/main/java/aoc/dia1/Day01.java
    public String part1(String input) {
        List<Rotation> rotations = Parser.parse(input);
        Dial dial = new Dial();
        int count = 0;

        for (Rotation rotation : rotations) {
            dial.apply(rotation);
            if (dial.isZero()) {
                count++;
            }
        }

        return String.valueOf(count);
    }
```

- **Parte 1** usa `apply()` (salto directo al final de la rotación) y comprueba
  si quedó en `0`.
- **Parte 2** usa `countZerosDuring()` (simulación clic a clic) que cuenta cada
  paso por `0`, cubriendo el caso de varias vueltas completas (p. ej. `R1000`).

## 7. Resultados verificados

| Entrada | Parte 1 | Parte 2 |
|---------|---------|---------|
| Ejemplo del enunciado (10 líneas) | 3 | 6 |
| Input real (`Input01.txt`) | 980 | 5961 |

## 8. Posibles mejoras

- `Parser.parseLine(...)` es un método privado sin usar (la lógica vive en el
  `map` del `parse`); podría eliminarse o reutilizarse para reducir duplicación.
- `Parser` y `Dial` están listos para tests unitarios aislados gracias a la
  separación de responsabilidades.
- `DayRegistry.get` devuelve `null` si el día no existe; se podría lanzar una
  excepción descriptiva o devolver `Optional<Day>`.

## 9. Defensa del desarrollo (preguntas frecuentes)

Esta sección recoge, en formato pregunta-respuesta, cómo justificaría las
decisiones de diseño ante alguien que revisa el desarrollo.

**¿Por qué separar `part1` y `part2` si comparten lógica?**
Porque no son dos arquitecturas distintas: son los **dos retos** que Advent of
Code plantea cada día. La estructura `Day.part1 / Day.part2` refleja literalmente
el enunciado. Ambos reutilizan el mismo dominio (`Parser` + `Dial`); lo único que
cambia es la pregunta que responden, así que no hay duplicación de lógica de
negocio, solo dos puntos de entrada al mismo modelo.

**¿Por qué la lógica del dial está en `Dial` y no en `Day01`?**
Para mantener un **modelo de dominio rico**. El dial tiene estado (su posición) y
reglas (el wrap-around circular módulo 100). Si esa lógica viviera en `Day01`,
`Day01` tendría dos motivos para cambiar (orquestar y calcular) y se rompería el
principio de responsabilidad única. Manteniéndola en `Dial`, el conocimiento está
encapsulado y se puede probar de forma aislada.

**¿Qué pasa si mañana añades el Día 2? ¿Qué tienes que tocar?**
Solo dos cosas: crear `Day02 implements Day` con su lógica y registrarlo en el
`Map` del `DayRegistry`. No se modifica `DayRunner`, `InputReader`, `Day` ni nada
existente. Eso es el **principio abierto/cerrado**: el sistema está abierto a
extensión, cerrado a modificación.

**¿Por qué `DayRunner` recibe sus dependencias por constructor en vez de crearlas?**
Por **inyección de dependencias**. Si `DayRunner` hiciera `new InputReader()`
dentro, quedaría acoplado a esa implementación concreta y sería difícil de
testear. Recibiéndolas por constructor puedo pasarle dobles de prueba (un
`InputReader` que devuelva un texto fijo, por ejemplo) sin tocar disco.

**¿Por qué `DayRunner` depende de la interfaz `Day` y no de `Day01`?**
Por la **inversión de dependencias**: los módulos de alto nivel no deben depender
de los de bajo nivel, sino de abstracciones. `DayRunner` orquesta cualquier día
hablando con el contrato `Day`; no necesita saber que existe `Day01`.

**¿Por qué un `enum` para la dirección y un `record` para la rotación?**
El `enum Direction` elimina los caracteres mágicos `'L'`/`'R'` y da seguridad de
tipos: el compilador impide valores inválidos. El `record Rotation` representa un
dato inmutable; una vez creado no cambia, lo que evita efectos secundarios y hace
el flujo de datos más predecible.

**¿Por qué `InputReader` lee desde el classpath y no con una ruta de fichero?**
Porque una ruta relativa (`src/main/resources/...`) depende del directorio desde
el que se lance el programa y se rompe fácilmente. Leyendo el recurso del
classpath (`getResourceAsStream`) funciona igual en el IDE y empaquetado en un
JAR, y es independiente del directorio de trabajo.

**¿Cómo manejas el wrap-around circular del dial?**
Con aritmética modular. A la derecha: `(position + steps) % 100`. A la izquierda,
restar puede dar negativo, así que se normaliza con
`((position - steps) % 100 + 100) % 100` para garantizar un resultado en `[0, 99]`.

**¿Por qué en la Parte 2 simulas clic a clic en vez de usar una fórmula?**
Por **claridad y corrección**. La simulación cuenta de forma natural cada paso por
`0`, incluyendo el caso en que una sola rotación da varias vueltas completas
(p. ej. `R1000` pasa por `0` diez veces). Una fórmula cerrada sería más rápida,
pero con estos tamaños de entrada la simulación es suficiente y deja la intención
mucho más legible. Es un compromiso consciente entre rendimiento y legibilidad.

**¿Cómo probarías este código?**
Las clases están desacopladas, así que se pueden testear aisladas: `Parser` con
un texto de ejemplo comprobando la lista de `Rotation`; `Dial` aplicando
rotaciones concretas y verificando la posición y el conteo de ceros; y `Day01`
con el ejemplo oficial del enunciado (que debe dar 3 y 6).
