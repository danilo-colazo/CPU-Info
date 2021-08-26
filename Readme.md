# CPU-Info

## *Aplicación de escritorio escrita en lenguaje java cuya finalidad es la de obtener la información del hardware y software de la CPU sobre la cual corre dicho programa.*

### El proyecto en si no solo tiene la finalidad de obtener la información anteriormente nombrada si no también de almacenarla en una base de datos.

La lógica del programa es la siguiente:
  * Cuando se ejecuta el programa se obtiene la información de la CPU, entre ellas la dirección MAC de la placa de red (utilizada como clave en la base de datos). 
  * Luego se hace una consulta a la base de datos para ver si la información de dicha CPU ya existe. 
  * En caso de existir se chequea que no haya habido ningún cambio. De lo contrario se actualiza la información. 
  * En caso de que no exista la información de la CPU en cuestión se la agrega en una nueva tupla.

