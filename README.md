# exercicio_concorrencia_2023.1

# Java Swing Music Player

Este é um aplicativo de player de música desenvolvido em Java Swing. Ele permite adicionar músicas a uma playlist, reproduzir, pausar e parar a reprodução, bem como remover músicas da lista de reprodução.

## Requisitos

- Java Development Kit (JDK) instalado
- Bibliotecas `javazoom.jl.decoder` e `javazoom.jl.player` para reprodução de áudio

**Nota**: A única linguagem de programação permitida para uso é Java, mantendo a JDK e nível de linguagem que vierem configuradas no repositório (Oracle OpenJDK 20 e nível de linguagem 20), e a única biblioteca permitida para a interface gráfica é Java Swing, não sendo permitido o uso do JavaFX, por exemplo.
    Além disso, as músicas devem estar no formato MP3 para serem reproduzidas pelo aplicativo.

## Recursos
- Adicionar músicas à lista de reprodução.
- Remover músicas da lista de reprodução.
- Reproduzir/pausar música atual.
- Parar a reprodução.
- Avançar para a próxima ou anterior música.
- Repetir a reprodução.
- Embaralhar a lista de reprodução.
- Exibir metadados da música, como título, álbum e artista.
- Controlar o progresso da reprodução por meio de um "scrubber".

## Como usar

1. Compile e execute o código Java.
2. Uma janela de interface do usuário será exibida.
<img width="714" alt="interface" src="https://github.com/if677/exercicio-de-concorrencia-2023-2-9-avbl-snon/assets/53984571/2a374c9f-0a3c-4cc7-abff-b07e24cda2c4">

3. Você pode adicionar músicas à playlist usando o botão "Adicionar Música".
4. Selecione uma música na lista de reprodução e clique em "Reproduzir" para iniciar a reprodução.

<img width="715" alt="Screenshot 2024-03-03 at 19 23 32" src="https://github.com/if677/exercicio-de-concorrencia-2023-2-9-avbl-snon/assets/53984571/2f461dcc-60b5-40c3-a323-33b71fc38479">


5. Você pode pausar a reprodução clicando em "Pausar" e retomá-la clicando novamente em "Reproduzir".
6. Para reproduzir a próxima música da lista, clique no botão com o ícone "Próxima".
7. Para reproduzir a música anterior da lista, clique no botão com o ícone "Anterior".
8. Para parar a reprodução, clique em "Parar".
9. Para remover uma música da lista de reprodução, selecione-a na lista e clique em "Remover".

## Embaralhar
- Clique no botão "Embaralhar" para randomizar a ordem das músicas na lista de reprodução.
- Clicar novamente reverterá para a ordem original.

## Notas adicionais

- Este player de música suporta reprodução de uma lista de músicas em sequência.
- As funcionalidades de "Próxima", "Anterior", "Aleatório" foram implementadas na versão atual.
- A reprodução de áudio é realizada utilizando as bibliotecas `javazoom.jl.decoder` e `javazoom.jl.player`.
- Os metadados das músicas (título, álbum, artista, ano, duração) são exibidos na interface do usuário.
- O tempo de reprodução da música é exibido em um controle deslizante (scrubber) na interface do usuário.

## Observações

- Algumas funcionalidades podem estar sujeitas a bugs, como relatado nos comentários do código.
- Este projeto foi desenvolvido como parte de um trabalho acadêmico (SEGUNDA ENTREGA), e esta em fase de desenvolvimento ou teste.

Autor: Antonio(avbl) e Stela(snon)

Data: 03/03/2024

Versão: 2.0

