# 🚀 DACD Data App - Sprint 1

Aplicación de extracción de datos en tiempo real desarrollada en Java. Este sistema captura información de múltiples fuentes mediante APIs REST y persiste los datos de forma concurrente en una base de datos SQLite centralizada.

## 🏗️ Arquitectura del Proyecto

El proyecto sigue una estructura multi-módulo gestionada con Maven para garantizar el desacoplamiento:

* **`spacex-extractor`**: Módulo encargado de conectarse a la API de SpaceX para extraer y almacenar la telemetría de los satélites Starlink.
* **`weather-extractor`**: Módulo encargado de conectarse a OpenWeatherMap para capturar las condiciones meteorológicas locales (ej. Las Palmas de Gran Canaria).
* **`spacex_data.db`**: Base de datos SQLite compartida donde ambos motores escriben sus observaciones de forma segura.

## ⚙️ Requisitos
* Java 21 o superior.
* Maven instalado.
* API Key válida de [OpenWeatherMap](https://openweathermap.org/api) (Reemplazar en `weather-extractor/src/.../Main.java`).

## ▶️ Cómo ejecutar la aplicación (Prueba de Integración)

Para simular el entorno de captura de datos en paralelo, debes iniciar ambos recolectores:

1. **Compilar el proyecto**:
   Ejecuta `mvn clean install` en la raíz del proyecto para asegurar que las dependencias entre módulos estén enlazadas.

2. **Iniciar Hilo 1 (Satélites)**:
   Navega a `spacex-extractor/.../Main.java` y ejecuta la aplicación. Comenzará a registrar la posición de los satélites cada minuto.

3. **Iniciar Hilo 2 (Clima)**:
   Navega a `weather-extractor/.../Main.java` y ejecuta la aplicación **sin detener la anterior**. Registrará el clima cada 15 minutos.

4. **Verificación**:
   Abre el archivo `spacex_data.db` con una herramienta como *DB Browser for SQLite*. Verás que las tablas `starlink_raw` y `weather_raw` se alimentan simultáneamente sin bloqueos ni colisiones.

---
*Desarrollado para la asignatura Ciencia e Ingeniería de Datos. Pablo Mellado y Yone Suárez*