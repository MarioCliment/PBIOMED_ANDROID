# PBIOMED_ANDROID

## Descripción del Proyecto

Este repositorio forma parte del proyecto PBIOMED (Biometría y Medio Ambiente) y contiene la aplicación Android desarrollada para interactuar con dispositivos Bluetooth Low Energy (BLE). La aplicación se encarga de escanear y conectarse a dispositivos BLE cercanos, y también realiza peticiones REST a un servidor para almacenar datos de medición.

## Contenido del Repositorio

- `MainActivity.java`: El archivo principal de la aplicación Android que gestiona el escaneo y la conexión a dispositivos BLE, así como las peticiones REST.
- `PeticionarioREST.java`: Una clase que se encarga de realizar peticiones REST al servidor.
- Otros archivos y recursos necesarios para la aplicación Android.

### Peticiones REST

- La aplicación Android puede realizar peticiones REST al servidor para guardar datos de medición.
- Utiliza la clase `PeticionarioREST` para hacer peticiones POST al servidor en una URL específica.

## Uso de la Aplicación

Antes de utilizar la aplicación, asegúrate de cumplir con los siguientes requisitos:

- Tener un dispositivo Android con soporte para BLE.
- Habilitar el permiso de ubicación en tu dispositivo.
- Asegurarte de que el Bluetooth de tu dispositivo esté habilitado.

Para utilizar la aplicación:

1. Abre la aplicación en tu dispositivo Android.
2. Pulsa el botón "Buscar Dispositivos BTLE" para escanear dispositivos cercanos.
3. La aplicación mostrará información sobre los dispositivos BLE detectados, incluyendo su nombre y otros detalles.
4. Puedes detener el escaneo pulsando el botón "Detener Búsqueda de Dispositivos BTLE".
5. Si deseas buscar un dispositivo BLE específico, puedes hacerlo pulsando el botón "Buscar Nuestro Dispositivo BTLE" e introduciendo el nombre del dispositivo deseado.
6. Una vez que se detecte el dispositivo, la aplicación mostrará información detallada sobre él.
7. La aplicación también realiza peticiones REST al servidor para almacenar datos de medición. Asegúrate de que el servidor esté configurado y disponible.

## Permisos de la Aplicación

La aplicación requiere permisos de ubicación y Bluetooth para funcionar correctamente. Asegúrate de otorgar estos permisos cuando se te solicite.

## Autor

- [@MarioCliment](https://www.github.com/MarioCliment)
