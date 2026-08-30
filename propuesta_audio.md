[opsx-propose](recipe;file:///home/nov0caina/Documents/VSC_Env/Workspace/buscaminas-del-rancho/.agent/workflows/opsx-propose.md) [openspec-propose](slashCommand;openspec-propose) [game-feel](slashCommand;game-feel)  Ahora ha llegado el momento de agregar sonido al videojuego, tengo una carpeta con todos los efectos de sonido y el soundtrack de fondo, se encuentra en la ruta: /home/nov0caina/Music/Buscaminas Del Rancho/

Descripción tecnica de los sonidos y cuando se ejecutaran:

Trigger: Explotar una mina (perder).
Sonidos: 
Comportamiento:

Trigger: Dar click a una celda con una mina (perder).
Ruta efectos: /home/nov0caina/Music/Buscaminas Del Rancho/LoseGame_SoundEffect/
Comportamiento: Cadda que un jugador pierde se reproducirá la animación de explosión, acompañada de un sonido aleatorio de explosion y seguido rapidamente por un sonido de Lose_Funny.
Ejemplo: reproducir de forma aleatoria los sonidos de Explotion_01.wav y Lose_FunnyTrumpet02.wav

Trigger: Dar click en un boton o celda.
Ruta efectos: /home/nov0caina/Music/Buscaminas Del Rancho/PopCell_SoundEffect/
Sonido: Pop_Double02.wav 
Comportamiento: al darr click en algun boton en el menu o sub opciones o celda en el juego se reproducira el sonido Pop_Double2.wav al unisono con la animacion del boton o celda.
Cuando se trata de una celda en el juego se reproduciran a la vez los sonidos Pop_Double02.wav y el sonido Pop_01.wav.

Trigger: Despejar todo el terreno (Victoria).
Ruta efectos: /home/nov0caina/Music/Buscaminas Del Rancho/Win_SoundEffects/
Sonidos: Victory_Woooow.wav seguido de Victory_Celebration1.mp3 o Victory_Celebration02.mp3 de forma aleatoria.
Comportamiento: Cuando el jugador despeja todo el terreno, gana el juego y se ejecuta la animación de victoria, brevemente antes de ejecutar la animación se debe comenzar a reproducir el sonido VIctory_Woooow.wav y justo antes de terminar de reproducir Victory_Woooow.wav se debe inciar la reproducción de Victory_Celebration1.mp3 o Victory_Celebration02.mp3 de forma aleatoria.

Trigger: Entrar al videojuego.
Ruta soundtrack: /home/nov0caina/Music/Buscaminas Del Rancho/Soundtrack music/
Sonidos: Soundtrack_BandaInstrumental_PistaLibreUso(Estilo  Sinaloense).mp3 y Soundtrack_CorridoTumbado_Instrumental_PistaLibreUso.mp3.
Comportamiento: al entrar al jjuego se iniciara con la reproducción de alguno de los dos soundtracks, en la pantalla de configuraciiones debe haber una opcion para establecer el nivel de la musica del soundtrack o desactivarla. La seleccion del soundtrack a reproducirr sera aleatorio al inciar y al finalizar la pista en curso se comenzará a rreproducir la siguiente con un efecto fade, de modo que nunca se quede en silencio y transicione de forma smooth.

Trigger: Largos periodos de espera en la pantalla de juego sin hacer nada.
Ruta efectos: /home/nov0caina/Music/Buscaminas Del Rancho/Waiting_SoundEffects/
Sonidos: Waiting_AFewMomentsLater.wav y Waiting_TwoHoursLater.wav.
Comportamiento: Despues de una espera considerable en la pantalla de juego sin mover se reproducirá alguno de los dos sonidos de forma aleatoria.

A tomar en cuenta: los sonidos estan en formato .mp3 y .wav, si necesitas optimizar los audios o convertirlos a otro formato, hazlo, transfierelos en orden al directorio de este poyecto.