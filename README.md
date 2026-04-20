# 🚀 DACD Data App - Sprint 2 (Completo)
Aplicación de extracción y distribución de datos en tiempo real desarrollada en Java. Este sistema captura información de múltiples fuentes mediante APIs REST y distribuye los datos utilizando una Arquitectura Orientada a Eventos (EDA) a través de un Message Broker centralizado.

## 💡 Propuesta de Valor (Visión del Proyecto)
El objetivo final de este datamart es alimentar un Monitor Predictivo de Cobertura Satelital (Rain Fade). El sistema cruzará la trayectoria en tiempo real de los satélites de la constelación Starlink con eventos de clima adverso local (lluvia, densidad de nubes) para predecir microcortes en la conexión a internet satelital, aportando valor a nómadas digitales y trabajadores remotos en zonas rurales de Canarias.

## 🏗️ Arquitectura del Proyecto (Orientada a Eventos)
El proyecto sigue una estructura multi-módulo gestionada con Maven. En este sprint, se ha eliminado el acoplamiento directo a la base de datos, separando el sistema en Productores y Suscriptores independientes:

**spacex-extractor (Productor)**: Módulo encargado de conectarse a la API de SpaceX para extraer la telemetría y posición orbital. Serializa los objetos a formato JSON (usando Gson) y publica los eventos en lotes (batch) en el Topic `sensor.SpaceX`.

**weather-extractor (Productor)**: Módulo encargado de conectarse a OpenWeatherMap para capturar las condiciones meteorológicas locales. Publica las observaciones serializadas en formato JSON en el Topic `prediction.Weather`.

**Message Broker (ActiveMQ)**: Actúa como el núcleo de comunicaciones (middleware). Recibe los eventos y los clasifica en canales, asegurando que la información fluya de forma asíncrona.

**event-store-builder (Suscriptor)**: Módulo consumidor que establece una *suscripción duradera* con el Broker. Escucha los canales y almacena los eventos en disco local siguiendo el formato NDJSON (un JSON por línea), estructurando automáticamente las carpetas bajo el formato: `eventstore/{topic}/{ss}/{YYYYMMDD}.events`.

### Diagrama de Clases UML
A continuación se detalla la estructura interna de los módulos, destacando la separación de responsabilidades entre la conexión a las APIs, el modelo de datos y la persistencia:

![Diagrama de Clases del Sistema](images/dacd-data-app.png)

## 🧩 Principios de Diseño Aplicados
Para asegurar la escalabilidad y mantenibilidad del código, se han aplicado los siguientes principios y patrones:

**Responsabilidad Única (SRP)**: Cada clase tiene un único propósito (ej. `FileEventStore` solo guarda en disco, el suscriptor solo escucha).
**Inversión de Dependencias (DIP)**: Los módulos dependen de abstracciones.
**Desacoplamiento Máximo (Patrón Pub/Sub)**: Los extractores no saben dónde ni cómo se guardan los datos, solo los publican en el Broker.
**Tolerancia a Fallos (Resiliencia)**: Se ha implementado el protocolo de reconexión automática (`failover`) en las conexiones a ActiveMQ, permitiendo que los módulos de Java sobrevivan a caídas del servidor de mensajería sin detener su ejecución.

## ⚙️ Requisitos
* Java 21 o superior (Variable `JAVA_HOME` configurada en el sistema).
* Maven instalado.
* Apache ActiveMQ (v5.15.x o superior) instalado en el equipo local.
* API Key válida de OpenWeatherMap (**Importante:** Debe estar configurada en el sistema operativo como variable de entorno bajo el nombre `OPENWEATHER_API_KEY` por motivos de seguridad).

## ▶️ Cómo ejecutar la aplicación
Dado que el sistema es distribuido, el orden de arranque es importante:

1. **Iniciar el Broker (ActiveMQ)**:
   Abre una consola en el directorio `bin` de tu instalación de ActiveMQ y ejecuta: `activemq start`. (Mantén la consola abierta).

2. **Compilar el proyecto**:
   Ejecuta `mvn clean install` en la raíz del proyecto para descargar dependencias y compilar los tres módulos.

3. **Iniciar el Suscriptor (Event Store Builder)**:
   Ejecuta la clase `Main` del módulo `event-store-builder`. Se quedará a la escucha de nuevos eventos.

4. **Ejecutar los Productores**:
   Ejecuta las clases `Main` de los módulos `spacex-extractor` y `weather-extractor`.

### Verificación y Monitorización:
* **Consola Web**: Entra en `http://localhost:8161/admin` (admin/admin), ve a "Topics" y observa el flujo de mensajes encolados y desencolados.
* **Almacenamiento Local**: Revisa la raíz del proyecto. Verás aparecer la carpeta `eventstore/` generada automáticamente con los datos particionados por origen y fecha.

---
*Desarrollado para la asignatura Desarrollo de aplicaciones para Ciencia de Datos. Pablo Mellado y Yone Suárez*