# 🚀 DACD Data App - Sprint 1

Aplicación de extracción de datos en tiempo real desarrollada en Java. Este sistema captura información de múltiples fuentes mediante APIs REST y persiste los datos de forma concurrente en una base de datos SQLite centralizada.

## 💡 Propuesta de Valor (Visión del Proyecto)
El objetivo final de este datamart es alimentar un **Monitor Predictivo de Cobertura Satelital (Rain Fade)**. El sistema cruzará la trayectoria en tiempo real de los satélites de la constelación Starlink con eventos de clima adverso local (lluvia, densidad de nubes) para predecir microcortes en la conexión a internet satelital, aportando valor a nómadas digitales y trabajadores remotos en zonas rurales de Canarias.

## 🏗️ Arquitectura del Proyecto

El proyecto sigue una estructura multi-módulo gestionada con Maven para garantizar el desacoplamiento:

* **`spacex-extractor`**: Módulo encargado de conectarse a la API de SpaceX para extraer y almacenar la telemetría y posición orbital.
* **`weather-extractor`**: Módulo encargado de conectarse a OpenWeatherMap para capturar las condiciones meteorológicas locales.
* **`spacex_data.db`**: Base de datos SQLite (Event Store) compartida donde ambos motores escriben sus observaciones en formato crudo (JSON) de forma segura.

### Diagrama de Clases UML
A continuación se detalla la estructura interna de los módulos, destacando la separación de responsabilidades entre la conexión a las APIs, el modelo de datos y la persistencia:

![Diagrama de Clases del Sistema](images/dacd-data-app.png)

## 🧩 Principios de Diseño Aplicados
Para asegurar la escalabilidad y mantenibilidad del código, se han aplicado los siguientes principios:
* **Responsabilidad Única (SRP):** Cada clase tiene un único propósito (ej. `OpenWeatherMapSupplier` solo extrae datos, no los guarda).
* **Inversión de Dependencias (DIP):** Los módulos principales no dependen de implementaciones concretas de bases de datos o APIs, sino de abstracciones (interfaces como `WeatherSupplier` y `EventStoreBuilder`).
* **Desacoplamiento:** La base de datos ignora el origen de los datos, limitándose a almacenar los registros crudos que le envían los *feeders*.

## ⚙️ Requisitos
* Java 21 o superior.
* Maven instalado.
* API Key válida de [OpenWeatherMap](https://openweathermap.org/api) (Reemplazar en `weather-extractor/src/.../Main.java`).

## ▶️ Cómo ejecutar la aplicación (Prueba de Integración)

Para simular el entorno de captura de datos en paralelo, debes iniciar ambos recolectores:

1. **Compilar el proyecto**:
   Ejecuta `mvn clean install` en la raíz del proyecto para asegurar que las dependencias entre módulos estén enlazadas.

2. **Iniciar Hilo 1 (Satélites)**:
   Navega a `spacex-extractor/.../Main.java` y ejecuta la aplicación. Comenzará a registrar la posición de los satélites periódicamente.

3. **Iniciar Hilo 2 (Clima)**:
   Navega a `weather-extractor/.../Main.java` y ejecuta la aplicación **sin detener la anterior**. Registrará el clima cada 15 minutos.

4. **Verificación**:
   Abre el archivo `spacex_data.db` con una herramienta como *DB Browser for SQLite*. Verás que las tablas `starlink_raw` y `weather_raw` se alimentan simultáneamente sin bloqueos ni colisiones.

---
*Desarrollado para la asignatura Ciencia e Ingeniería de Datos. Pablo Mellado y Yone Suárez*