import javazoom.jl.decoder.*;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import support.PlayerWindow;
import support.Song;

import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Player {

    // A capacidade máxima da playlist
    private static final int TAMANHO = 1000;

    // Nome do player
    private static final String TITULO = "Projeto Infra Software TERCEIRA ENTREGA - <avbl> - <snon>";

    // Informações metadados = "Title", "Album", "Artist", "Year", "Length", "Path"
    private static final int INFO_SIZE = 6;

    // As info extraidas do metadados da música
    private Song[] playlist = new Song[TAMANHO];

    // Only the headers
    private String[][] QUEUE_REPRODUCE = new String[TAMANHO][INFO_SIZE];

    // Rand Queue - Terceira entrega
    private String[][] RAND_QUEUE_REPRODUCE = new String[TAMANHO][INFO_SIZE];

    // Locks and conditions - Global Threads
    public Lock lock = new ReentrantLock();
    public Condition action = lock.newCondition();
    public boolean using = false;

    public boolean loop = false;
    public boolean pause = false;
    public boolean next = false;

    public boolean previous = false;
    public boolean random = false;

    private Bitstream bitstream;
    private Decoder decoder;
    private AudioDevice device;

    private PlayerWindow window;

    private int currentFrame = 0;

    Thread playNow = new Thread(new playNowThread());
    Thread remove = new Thread(new removeThread());
    Thread addSong = new Thread(new addSongThread());
    Thread playPause = new Thread(new playPauseThread());
    Thread stop = new Thread(new stopThread());
    Thread nextSong = new Thread(new nextThread());
    Thread previousSong = new Thread(new previousThread());
    Thread loopSong = new Thread(new loopThread());
    Thread randomSong = new Thread(new randomThread());


    // Threads para as funções do player - Botão de player, remove, AddSong, Play/Pause, Stop
    // Next, Previous, Shuffle, Loop agora foram implementadas.
    private final ActionListener buttonListenerPlayNow = e -> {
        try {
            playNow();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    };
    private final ActionListener buttonListenerRemove = e ->  remove();
    private final ActionListener buttonListenerAddSong = e ->  addSong();
    private final ActionListener buttonListenerPlayPause = e -> playPause();
    private final ActionListener buttonListenerStop = e -> stop();
    private final ActionListener buttonListenerNext = e -> nextSong();
    private final ActionListener buttonListenerPrevious = e ->  previousSong();
    private final ActionListener buttonListenerShuffle = e -> randomSong();
    private final ActionListener buttonListenerLoop = e -> loopSong();

    // Mouse Input Adapter para o scrubber
    private final MouseInputAdapter scrubberMouseInputAdapter = new MouseInputAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }
    };

    public Player() {
        EventQueue.invokeLater(() -> window = new PlayerWindow(
                TITULO,
                QUEUE_REPRODUCE,
                buttonListenerPlayNow,
                buttonListenerRemove,
                buttonListenerAddSong,
                buttonListenerShuffle,
                buttonListenerPrevious,
                buttonListenerPlayPause,
                buttonListenerStop,
                buttonListenerNext,
                buttonListenerLoop,
                scrubberMouseInputAdapter)
        );
    }


    // Return true or false
    private boolean playNextFrame() throws JavaLayerException {
        if (device != null) {
            Header h = bitstream.readFrame();
            if (h == null) return false;

            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);
            device.write(output.getBuffer(), 0, output.getBufferLength());
            bitstream.closeFrame();
        }
        return true;
    }

    private boolean skipNextFrame() throws BitstreamException {
        Header h = bitstream.readFrame();
        if (h == null) return false;
        bitstream.closeFrame();
        currentFrame++;
        return true;
    }

    private void skipToFrame(int newFrame) throws BitstreamException {
        if (newFrame > currentFrame) {
            int framesToSkip = newFrame - currentFrame;
            boolean condition = true;
            while (framesToSkip-- > 0 && condition) condition = skipNextFrame();
        }
    }


    private void playNow() throws InterruptedException {
        if (playNow.isAlive()) {
            playNow.interrupt();
            TimeUnit.SECONDS.sleep(2);
        }
        playNow = new Thread(new playNowThread());
        playNow.start();
    }

    private void remove(){
        if (remove.isAlive()) {
            remove.interrupt();
        }
        remove.run();
    }
    private void addSong(){
        if (addSong.isAlive()) {
            addSong.interrupt();
        }
        addSong.run();
    }
    private void playPause(){
        if (playPause.isAlive()) {
            playPause.interrupt();
        }
        playPause.run();
    }
    private void stop(){
        if (stop.isAlive()) {
            stop.interrupt();
        }
        stop.run();
    }
    private void nextSong(){
        if (nextSong.isAlive()) {
            nextSong.interrupt();
        }
        nextSong.run();
    }
    private void previousSong(){
        if (previousSong.isAlive()) {
            previousSong.interrupt();
        }
        previousSong.run();
    }
    private void loopSong() {
        if (loopSong.isAlive()) {
            loopSong.interrupt();
        }
        loopSong.run();
    }

    private void randomSong(){
        if (randomSong.isAlive()) {
            randomSong.interrupt();
        }
        randomSong.run();
    }

    // Add music to playlist, mudamos essa função para a thread de reprodução não parar enquanto estamos adicionando
    // uma nova música na playlist.
    class addSongThread implements Runnable {
        public void run() {
            // Essa func agenda a execução do código dentro do método run() pra ser executado no evento Dispatch da Thread (EDT)
            // do Swing. É importante fazer isso pq a operação openFileChooser() e a att da interface do usuário devem
            // ocorrer na EDT pra evitar problema de sincronização entre a thread de adicionar música e a manipulação da UI.
            EventQueue.invokeLater(() -> {

                // Abre o seletor de arquivos para que o usuário possa escolher uma música para adicionar.
                // Essa operação é potencialmente bloqueante, então executá-la na EDT previne que a interface do usuário
                // congele durante a operação.

                // Mesmo método de abrir o seletor de arquivos para o user escolher uma música para adicionar.
                // O ponto chave está aqui, essa operação é potencialmente bloqueante, então executá-la na EDT previne
                // que a interface do usuário congele durante a operação.
                Song song = window.openFileChooser();
                if (song != null) {

                    // Percorre o array playlist para encontrar o primeiro espaço null (indicando um espaço vazio onde
                    // uma nova música pode ser adicionada).
                    for (int i = 0; i < TAMANHO; i++) {
                        if (playlist[i] == null) {
                            // Adiciona a música selecionada pelo usuário ao primeiro espaço vazio encontrado na playlist.
                            playlist[i] = song;

                            // Copia as informações de exibição da música para a fila de reprodução (QUEUE_REPRODUCE),
                            // que é usada para atualizar a interface do usuário com as músicas na playlist.
                            System.arraycopy(song.getDisplayInfo(), 0, QUEUE_REPRODUCE[i], 0, INFO_SIZE);

                            // Atualiza a lista de reprodução e tudo mais
                            window.setQueueList(QUEUE_REPRODUCE);

                            // Sai do loop.
                            break;
                        }
                    }
                }
            });
        }
    }


    // Remove music from playlist
    class removeThread implements Runnable {
        public void run() {
            lock.lock();
            while (using) {
                try {
                    action.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            using = true;
            String id = window.getSelectedSongID();

            for (int i = 0; i < TAMANHO; i++) {
                if (Objects.equals(QUEUE_REPRODUCE[i][5], id)) {
                    playlist[i] = null;
                    System.arraycopy(new String[INFO_SIZE], 0, QUEUE_REPRODUCE[i], 0, INFO_SIZE);
                    for (int j = i; j < TAMANHO - 1; j++) {
                        System.arraycopy(QUEUE_REPRODUCE[j + 1], 0, QUEUE_REPRODUCE[j], 0, INFO_SIZE);
                        playlist[j] = playlist[j + 1];
                    }
                    break; // Adicionado para parar o loop uma vez que a música foi removida.
                }
            }
            window.setQueueList(QUEUE_REPRODUCE);
            using = false;
            action.signalAll();
            lock.unlock();
        }
    }

    class playNowThread implements Runnable {
        public void run() {
            Song song = null;
            try {
                // Tenta adquirir o lock antes de proceder para garantir que não haja concorrência indesejada
                lock.lock();

                // Espera até que o recurso não esteja sendo usado por outra thread
                while (using) {
                    try {
                        action.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Marca o recurso como sendo usado
                using = true;
                pause = false; // Reinicia o estado de pausa

                // Obtém o ID da música selecionada atualmente na interface do usuário
                String id = window.getSelectedSongID();

                // System.out.println("ID: " + id);

                // Recupera a música correspondente ao ID
                song = getSong(id);

                // Se uma música foi selecionada, atualiza a interface do usuário com suas informações
                if (song != null) {
                    window.setPlayingSongInfo(song.getTitle(), song.getAlbum(), song.getArtist());
                    // Habilita os botões de controle na interface do usuário

                    window.setEnabledLoopButton(true);
                    window.setEnabledScrubber(true);
                    window.setEnabledNextButton(true);
                    window.setEnabledPlayPauseButton(true);
                    window.setPlayPauseButtonIcon(1); // Ícone de pausa
                    window.setEnabledStopButton(true);
                    window.setEnabledPreviousButton(true);
                    window.setEnabledShuffleButton(true);
                }

                // Inicializa o dispositivo de áudio e o decodificador
                device = FactoryRegistry.systemRegistry().createAudioDevice();
                device.open(decoder = new Decoder());
                // Prepara o bitstream da música para reprodução
                bitstream = new Bitstream(song.getBufferedInputStream());
                currentFrame = 0; // Reseta o contador de frames para a nova música
                using = false; // Libera o recurso para outras threads
                action.signalAll(); // Notifica outras threads esperando pelo recurso
                lock.unlock(); // Libera o lock

                // Loop principal de reprodução
                while (true) {
                    lock.lock();
                    while (using) {
                        try {
                            action.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    using = true;

                    String nextId = null;
                    String previousId = null;

                    // Determina os IDs da próxima e da música anterior na playlist
                    for (int i = 0; i < TAMANHO - 1; i++) {
                        if (Objects.equals(QUEUE_REPRODUCE[i][5], id)) {
                            if (QUEUE_REPRODUCE[i + 1][5] == null) {
                                if (loop) {
                                    nextId = QUEUE_REPRODUCE[0][5];
                                    break;
                                } else {
                                    nextId = null;
                                    break;
                                }
                            }

                            nextId = QUEUE_REPRODUCE[i + 1][5];
                            break;
                        }
                    }

                    for (int i = 0; i < TAMANHO; i++) {
                        if (Objects.equals(QUEUE_REPRODUCE[i][5], id)) {
                            if (i == 0) {
                                previousId = null;
                                break;
                            }
                            previousId = QUEUE_REPRODUCE[i - 1][5];
                            break;
                        }
                    }

                    if (nextId != null) {
                        window.setEnabledNextButton(true);
                        if (next) {
                            // Quero printar no terminal que entrou no next
                            // System.out.println("Entrou no next");
                            next = false;
                            id = nextId;
                            song = getSong(id);

                            if (song != null) {
                                window.setPlayingSongInfo(song.getTitle(), song.getAlbum(), song.getArtist());
                                window.setEnabledLoopButton(true);
                                window.setEnabledScrubber(true);
                                window.setEnabledNextButton(true);
                                window.setEnabledPlayPauseButton(true);
                                window.setPlayPauseButtonIcon(1);
                                window.setEnabledStopButton(true);
                                window.setEnabledPreviousButton(true);
                                window.setEnabledShuffleButton(true);
                            }

                            device = FactoryRegistry.systemRegistry().createAudioDevice();
                            device.open(decoder = new Decoder());
                            bitstream = new Bitstream(song.getBufferedInputStream());
                            currentFrame = 0; // Reseta o contador de frames para a nova música
                            window.setTime((int) (currentFrame * song.getMsPerFrame()), (int) song.getMsLength());
                        }
                    } else {
                        window.setEnabledNextButton(false);
                    }

                    if (previousId != null) {
                        window.setEnabledPreviousButton(true);
                        if (previous) {
                            // System.out.println("Entrou no previous");
                            previous = false;
                            id = previousId;
                            song = getSong(id);

                            if (song != null) {
                                window.setPlayingSongInfo(song.getTitle(), song.getAlbum(), song.getArtist());
                                window.setEnabledLoopButton(true);
                                window.setEnabledScrubber(true);
                                window.setEnabledNextButton(true);
                                window.setEnabledPlayPauseButton(true);
                                window.setPlayPauseButtonIcon(1);
                                window.setEnabledStopButton(true);
                                window.setEnabledPreviousButton(true);
                                window.setEnabledShuffleButton(true);
                            }

                            device = FactoryRegistry.systemRegistry().createAudioDevice();
                            device.open(decoder = new Decoder());
                            bitstream = new Bitstream(song.getBufferedInputStream());
                            currentFrame = 0;
                            window.setTime((int) (currentFrame * song.getMsPerFrame()), (int) song.getMsLength());
                        }
                    } else {
                        window.setEnabledPreviousButton(false);
                    }

                    if (!pause) {
                        if (!playNextFrame()) {
//                            System.out.println("Entrando correto no pause");
                            next = false;
                            id = nextId;
                            song = getSong(id);
                            if (nextId != null) {
                                if (song != null) {
//                                    System.out.println("Entrou para resetar as informações da tela.");
                                    window.resetMiniPlayer(); // Reset all the information on the screen
                                    window.setPlayingSongInfo(song.getTitle(), song.getAlbum(), song.getArtist());
                                    window.setEnabledLoopButton(true);
                                    window.setEnabledScrubber(true);
                                    window.setEnabledNextButton(true);
                                    window.setEnabledPlayPauseButton(true);
                                    window.setPlayPauseButtonIcon(1);
                                    window.setEnabledStopButton(true);
                                    window.setEnabledPreviousButton(true);
                                    window.setEnabledShuffleButton(true);
                                }

                                device = FactoryRegistry.systemRegistry().createAudioDevice();
                                device.open(decoder = new Decoder());
                                assert song != null;
                                bitstream = new Bitstream(song.getBufferedInputStream());
                                currentFrame = 0; // Start the next song from the beginning
                            }
                        }

                        currentFrame += 1;
                    }

                    int time = (window.getScrubberValue());
                    assert song != null;
                    int frame = (int) (time / song.getMsPerFrame());

                    if (currentFrame < frame + 1) {
                        skipToFrame(frame);
                        currentFrame = frame;
                    }

                    // Se a música tiver terminado quero resetar tudo para a próxima música, porém, quando o shuffer é ativado a música começa bugada
                    if (buttonListenerShuffle.equals(false)) {
                        if (currentFrame >= song.getNumFrames()) {
                            next = true;
                        }
                    }

                    window.setTime((int) (currentFrame * song.getMsPerFrame()), (int) song.getMsLength());

                    using = false;
                    action.signalAll();
                    lock.unlock();
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            } catch (JavaLayerException | FileNotFoundException | InterruptedException ignored) {
            }
        }
    }

    class playPauseThread implements Runnable {
        public void run() {
            lock.lock();
            while (using) {
                try {
                    action.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            using = true;
            if (pause) {
                window.setPlayPauseButtonIcon(1);
                pause = false;
            } else {
                window.setPlayPauseButtonIcon(0);
                pause = true;
            }
            using = false;
            action.signalAll();
            lock.unlock();
        }
    }

    class stopThread implements Runnable {
        public void run() {
            lock.lock();
            while (using) {
                try {
                    action.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            using = true;
            device = null;
            bitstream = null;
            window.resetMiniPlayer();
            currentFrame = 0;
            pause = true;
            using = false;
            action.signalAll();
            lock.unlock();
        }
    }

    class nextThread implements Runnable {
        public void run() {
            lock.lock();
            while (using) {
                try {
                    action.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            using = true;
            next = true;
            using = false;
            action.signalAll();
            lock.unlock();
        }
    }

    class previousThread implements Runnable{
        public void run(){

            lock.lock();
            while (using) {
                try {
                    action.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            using = true;
            previous = true;
            using = false;
            action.signalAll();
            lock.unlock();
        }
    }

    class loopThread implements Runnable{
        public void run(){
            lock.lock();
            while (using) {
                try {
                    action.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            using = true;
            if (loop){
                loop = false;
                window.setEnabledLoopButton(true);
            }
            else {
                loop = true;
                window.setEnabledLoopButton(true);
            }
            using = false;
            action.signalAll();
            lock.unlock();
        }
    }

    class randomThread implements Runnable {
        public void run() {
            lock.lock();
            while (using) {
                try {
                    action.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            using = true;

            // Terceira entrega - Random Queue Reproduce

            // Se botão ta ativo e eu quero desativar voltando para oq tava antes
            if (random) {
                for (int i = 0; i < TAMANHO; i++) {
                    System.arraycopy(RAND_QUEUE_REPRODUCE[i], 0, QUEUE_REPRODUCE[i], 0, INFO_SIZE);
                }
                random = false;
            }

            else {

                int counter = 0;
                ArrayList<Integer> aux = new ArrayList<>();
                for (int i = 0; i < TAMANHO; i++) {
                    aux.add(i);
                    System.arraycopy(QUEUE_REPRODUCE[i], 0, RAND_QUEUE_REPRODUCE[i], 0, INFO_SIZE);
                }

                Collections.shuffle(aux);

                for (int i = 0; i < TAMANHO; i++) {
                    if (RAND_QUEUE_REPRODUCE[aux.get(i)][0] != null) {
                        System.arraycopy(RAND_QUEUE_REPRODUCE[aux.get(i)], 0, QUEUE_REPRODUCE[counter++], 0, INFO_SIZE);
                    }
                }

                random = true;
            }
            window.setQueueList(QUEUE_REPRODUCE);
            using = false;
            action.signalAll();
            lock.unlock();
        }
    }

    private Song getSong(String id){
        for(int i = 0; i < TAMANHO; i++){
            if(Objects.equals(QUEUE_REPRODUCE[i][5], id)){
                for(int j = 0; j < TAMANHO; j++) {
                    // Pegando a música correta de playlist (a música propriamente dita que agt adicionou)
                    if(QUEUE_REPRODUCE[i][5].equals(playlist[j].getUuid())) {
                        return playlist[j];
                    }
                }
            }
        }
        return null;
    }
}
