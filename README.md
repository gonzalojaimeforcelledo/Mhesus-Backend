# MHESUS API — Backend Java (Spring Boot)

API REST del taller MHESUS, implementada según el Design Doc v1.0 (sección 6),
con Spring Boot 3 + Spring Data JPA + Spring Security (JWT) + **PostgreSQL en
Supabase**. El código está organizado en **capas DDD** (Domain-Driven Design)
por módulo/bounded context, siguiendo la sección 4 del Design Doc.

## Estructura DDD

Cada módulo de negocio es un paquete independiente bajo `com.mhesus.api.`,
con sus propias 4 capas (Presentation → Application → Domain, e
Infrastructure implementando lo que Domain define — la Dependency Rule de
la sección 4.1 del Design Doc):

```
com.mhesus.api/
├── shared/               # Shared Kernel: utilidades y DTOs comunes a todos los módulos
│   ├── util/              (IdGenerator)
│   └── dto/                (ErrorResponse)
├── config/                # Configuración de infraestructura transversal
│                            (SecurityConfig, SeedDataRunner)
├── auth/                  # Autenticación, sesión, RBAC
│   ├── domain/             (Usuario, UsuarioRepository)
│   ├── application/        (AuthService, UsuarioService, DTOs)
│   ├── infrastructure/     (JwtUtil, JwtAuthFilter)
│   └── presentation/       (AuthController, UsuarioController)
├── clientes/               # Clientes y motocicletas
│   ├── domain/             (Cliente, Motocicleta, sus repositorios)
│   ├── application/        (ClienteService, DTOs)
│   └── presentation/       (ClienteController, MotoController)
├── ot/                     # Órdenes de trabajo — ciclo de vida completo
│   ├── domain/             (OrdenTrabajo, Diagnostico, EstadoOtUtil — la
│                             máquina de estados vive aquí: es una regla de
│                             negocio pura, no conoce Spring ni la BD)
│   ├── application/        (OtService, DTOs)
│   └── presentation/       (OtController)
├── almacen/                # Catálogo, pedidos, despacho, inventario
│   ├── domain/             (Producto, PedidoAlmacen, PedidoDetalle,
│                             MovimientoInventario, sus repositorios)
│   ├── application/        (ProductoService, PedidoService, DTOs)
│   └── presentation/       (ProductoController, PedidoController)
├── cotizacion/             # Cotización y autorización del cliente
│   ├── domain/             (Cotizacion, CotizacionRepository)
│   ├── application/        (CotizacionService, DTOs)
│   └── presentation/       (CotizacionController)
└── soporte/                 # Shared kernel operativo: auditoría y notificaciones
    ├── domain/             (RegistroAuditoria, Notificacion, sus repositorios)
    ├── application/        (SoporteService)
    └── presentation/       (SoporteController)
```

**Por qué no hay una capa `infrastructure/` separada en todos los módulos**:
en Spring Data JPA, una interfaz de repositorio (ej. `ClienteRepository
extends JpaRepository<...>`) ES el contrato de dominio — Spring genera la
implementación real (la "infraestructura") en tiempo de ejecución, así que no
hace falta escribirla a mano. Por eso los repositorios viven en `domain/`
junto a las entidades: siguen siendo la interfaz que el dominio define, sin
depender de detalles de JPA en su forma de uso. Los módulos `auth` sí tienen
`infrastructure/` explícita porque `JwtUtil`/`JwtAuthFilter` son
infraestructura real que si escribimos a mano (seguridad, no persistencia).

## Base de datos: Neon (ya conectado)

`application.yml` ya tiene la conexión completa hacia Neon:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://ep-broad-tree-axzli7t1-pooler.c-4.us-east-2.aws.neon.tech:5432/neondb?sslmode=require&channel_binding=require
    username: neondb_owner
    password: "npg_YkbnOj14pVix"
```

**Por qué Neon y no Supabase** (esto costó bastante diagnosticar, documentado
para no repetirlo): probamos la conexión directa de Supabase, el Transaction
Pooler y el Session Pooler — los tres fallaban con `SocketTimeoutException`.
Con `Test-NetConnection` de PowerShell confirmamos la causa real: **la red
del taller tiene IPv6 funcionando perfecto, pero no logra salir por IPv4**
(típico de conexiones de internet "IPv6-only", cada vez más comunes). Los
tres endpoints de Supabase resuelven a direcciones IPv4 — por eso ninguno
conectaba. El endpoint de Neon, en cambio, resolvió a una dirección IPv6 en
la prueba y conectó sin problema. Si en tu red SÍ tienes IPv4 funcionando
normal, Supabase debería funcionarte igual de bien — el problema no era
Supabase en sí, era esta combinación particular de red + proveedor.

No necesitas crear ninguna tabla a mano ni correr SQL en ningún panel —
**Flyway** gestiona el esquema automáticamente al arrancar la app, corriendo
las migraciones versionadas en `src/main/resources/db/migration/`:

- `V1__init_schema.sql` — las 12 tablas (agrupadas por módulo/bounded context)
- `V2__seed_data.sql` — usuarios, clientes, motos y productos de demostración

Por eso `spring.jpa.hibernate.ddl-auto` está en `validate` (no `update`):
Hibernate ya no crea ni modifica el esquema por su cuenta, solo **verifica**
que coincida con lo que esperan las entidades JPA — el esquema real lo
define Flyway, de forma explícita y versionada. Si alguna vez necesitas
cambiar el esquema, agrega un archivo nuevo `V3__algo.sql` (nunca edites
`V1`/`V2` una vez que ya corrieron contra una base real — Flyway detecta el
cambio y falla a propósito, para protegerte de modificar migraciones ya
aplicadas).

**Si cambias de proyecto de Neon** (o rotas la contraseña), consigue el
connection string nuevo desde tu panel de Neon → "Connect" → copia la URL
completa, y actualiza esas 3 líneas (`url`, `username`, `password`) en
`src/main/resources/application.yml` con los datos nuevos.

**Si más adelante quieres volver a intentar con Supabase** (por ejemplo, en
otra red con IPv4 funcionando), el connection string del Session Pooler es
la opción a usar — no la conexión directa ni el Transaction Pooler (ver el
historial de este README en versiones anteriores, o el botón "Connect" en
tu panel de Supabase → elige "Session pooler").

**Nota de seguridad**: para producción real, lo ideal es sacar `username` y
`password` del archivo y pasarlos por variable de entorno en vez de tenerlos
en texto plano dentro del repositorio — pero para desarrollo y pruebas locales
como este, tenerlos directos en el archivo es lo más simple y menos propenso
a errores (ya vimos varias veces que la sintaxis de variables de entorno
`${VAR}` genera confusión si no se usa exactamente bien).

## Cómo abrir este proyecto en tu IDE

Este proyecto ya incluye una carpeta `.idea/` con la configuración de Java 17
pre-fijada (`misc.xml`, `compiler.xml`), para que IntelliJ no tenga que
adivinar la versión — si al abrirlo tu IDE pregunta por un SDK "18" que no
tienes con ese nombre exacto, solo dale clic al selector y elige tu JDK 18
instalado (el nombre puede variar según el proveedor: Temurin, Semeru, etc.,
pero cualquier JDK 18 funciona).

El error más común ("no aparecen los archivos", "no detecta Spring Boot ni
JUnit") es abrir la carpeta equivocada. Después de descomprimir el ZIP, la
carpeta que **contiene directamente** `pom.xml` es `mhesus-backend-java/` — es
esa la que hay que abrir como raíz del proyecto, no la carpeta donde
descomprimiste el ZIP.

**IntelliJ IDEA** (el más usado para Spring Boot):
1. `File → Open...`
2. Selecciona la carpeta `mhesus-backend-java` (la que tiene `pom.xml` adentro)
3. Si aparece un aviso de "Maven project detected", dale a "Import" / "Load Maven Project"
4. Espera a que termine de indexar y descargar dependencias (barra de progreso abajo)
5. Ya deberías ver `MhesusApiApplication` con el ícono verde de Spring Boot (▶) y
   los tests con el ícono de JUnit para correrlos con clic derecho → Run

**Eclipse / Spring Tool Suite**: `File → Import → Maven → Existing Maven Projects`,
selecciona `mhesus-backend-java` y confirma. Los tests JUnit aparecen automáticamente
bajo `src/test/java` en el Package Explorer.

**VS Code**: instala el "Extension Pack for Java" y "Spring Boot Extension Pack",
luego `File → Open Folder...` sobre `mhesus-backend-java`. La barra lateral de
Java (ícono de taza de café) mostrará el árbol de fuentes, incluyendo `src/test/java`
con los tests JUnit listos para correr.

Si tu IDE sigue sin detectar nada, verifica primero que el **SDK del proyecto**
(`File → Project Structure → SDK`) y el **SDK de tu configuración de Run**
(`Run → Edit Configurations`) coincidan con la versión que pide `pom.xml`
(`<java.version>`, actualmente **18**) — Spring Boot 3 no arranca si mezclas
un JDK más viejo que el que se usó para compilar (verás un error tipo
`UnsupportedClassVersionError` si eso pasa). **Si cambiaste algo y ves ese
error igual, borra la carpeta `target/` a mano** — a veces el IDE no
recompila los `.class` viejos automáticamente.

## Correr las pruebas (JUnit)

```bash
mvn test
```

El proyecto incluye `src/test/java/com/mhesus/api/MhesusApiApplicationTests.java`
(prueba de humo: verifica que todo el contexto de Spring levanta sin errores)
y `src/test/java/com/mhesus/api/ot/domain/EstadoOtUtilTest.java` (pruebas
unitarias de la máquina de estados de la OT — vive junto a `EstadoOtUtil` en
`ot/domain`, siguiendo la nueva estructura). **Los tests usan H2 en memoria**
(ver `src/test/resources/application.yml`), no PostgreSQL — así corren rápido
y aislados, y no necesitas tener Supabase disponible solo para correr `mvn test`.

## Requisitos

- Java 17+ (el proyecto está configurado para Java 17 — si tu JDK es distinto,
  ajusta `<java.version>` en `pom.xml` y `Edit Configurations → SDK` en tu IDE
  para que coincidan)
- Maven 3.9+ (o usa el wrapper si lo agregas; este proyecto usa Maven del sistema)
- Nada más de base de datos — ya está conectado a Supabase (ver sección de arriba)
- Conexión a internet la primera vez, para que Maven descargue las dependencias desde Maven Central

## Ejecutar

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/api/v1`.

## Usuarios de demostración

Igual que el frontend: `recepcion`, `mecanico`, `mecanico2`, `almacen`, `jefe`, `admin`,
todos con contraseña `demo1234`.

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | /api/v1/auth/login | Login, devuelve JWT |
| GET/POST | /api/v1/clientes | Listar / crear clientes (`?dni=` para buscar) |
| GET/POST | /api/v1/clientes/{id}/motocicletas | Motos de un cliente |
| GET | /api/v1/motos?placa=&q= | Buscar moto por placa exacta o parcial |
| GET | /api/v1/motos/{id}/historial | Historial de OT de una moto |
| GET/POST | /api/v1/ot | Listar / crear OT |
| PATCH | /api/v1/ot/{id}/asignar, /estado, /avanzar, /finalizar-servicio | Ciclo de vida de la OT |
| POST/GET | /api/v1/ot/{id}/diagnostico | Diagnóstico del mecánico |
| GET/POST/PATCH/DELETE | /api/v1/productos | Catálogo de Almacén |
| POST | /api/v1/ot/{id}/pedidos | Generar pedido de repuestos |
| PATCH | /api/v1/pedidos/{id}/aprobar, /despachar | Flujo de aprobación y despacho |
| POST/GET | /api/v1/ot/{id}/cotizacion | Cotización |
| PATCH | /api/v1/cotizaciones/{id}/autorizar | Autorización del cliente |
| GET | /api/v1/usuarios, /auditoria | Administración |
| GET/PATCH | /api/v1/notificaciones/mias | Notificaciones del usuario logueado |

Todas las rutas requieren `Authorization: Bearer <token>` salvo `/auth/login`.

## Notas de arquitectura

- Los IDs son `String` (no autoincrementales) para calzar 1 a 1 con el frontend, que ya
  genera IDs con prefijo (ej. `cli_abc123`).
- La cotización guarda su detalle de ítems como JSON en una columna `TEXT` — es la forma
  más simple de mapear el array `ItemCotizacion[]` del frontend sin crear una tabla aparte.
- La máquina de estados de la OT (`ot.domain.EstadoOtUtil`) replica exactamente la
  secuencia de la sección 7 del Design Doc, y vive en `domain/` por ser una regla de
  negocio pura (no conoce Spring, JPA ni HTTP).
- CORS está configurado para `http://localhost:4200` (edítalo en `application.yml` si el
  frontend corre en otro origen).
- El esquema lo gestiona Flyway (`db/migration/`), con `ddl-auto: validate` —
  ver la sección "Base de datos" más arriba para el detalle.
