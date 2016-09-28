# primerForoSeguridad
Aplicacion creada para el primer foro del curso de Seguridad de la Informacion, del estudiante Eduardo Gamboa

# Como Compilar
El proyecto esta configurado para utilizar Maven, ademas de que esta escrito en el lenguaje Java version 8. Esto a fin de utilizar
caracteristicas especiales de dicha plataforma. Para compilarlo es necesario:
1. Obtener e instalar JDK version 1.8
1. Instalar la herramienta Maven
1. Desde una terminal, situarse en el directorio del proyecto
1. Ejecutar el comando `mvn package`

Al terminar el proceso, se creara una carpeta llamada `target/jfx/app/` se creara un archivo ejecutable en formato jar, y todas las bibliotecas
necesarias para la ejecucion de la aplicacion en el la carpeta `lib`