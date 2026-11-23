# Skin URL (Forge 1.7.10)

Troca a sua skin por um link e todo mundo que tiver o mod ve.

## Como usar no jogo
- `/skinurl <link>`  -> define sua skin (o link tem que abrir DIRETO a imagem .png)
- `/skinurl reset`   -> volta a skin normal

Seu link fica salvo em `config/skinurl.txt` e e reenviado sempre que voce entra num servidor.

## Instalar
- Coloque o `.jar` em `mods/` **no servidor e em cada cliente** que quiser ver as skins.
- Em singleplayer funciona sozinho.
- Cliente sem o mod entra normalmente, mas nao ve as skins trocadas.
- Servidor sem o mod: a skin muda so para voce.

## Compilar pelo GitHub (sem instalar nada)
1. Crie um repositorio no GitHub e envie TODOS estes arquivos (incluindo a pasta `.github`).
2. Abra a aba **Actions** -> workflow **Build mod** (roda sozinho a cada envio; ou clique em *Run workflow*).
3. Quando ficar verde, abra a execucao e baixe **SkinUrl-jar** em *Artifacts*. Dentro esta o `.jar`.

Se o build falhar, copie o erro da aba Actions e me mande.

## Limites
- Minecraft 1.7.10 nao tem braco fino (Alex): skins "slim" aparecem com braco normal.
- Imagens 64x32 (formato antigo) e 64x64 funcionam. Evite imagens enormes.
- Quem recebe a skin de outro jogador baixa a imagem direto do link dele, entao o dono do link
  consegue ver o IP de quem baixou. Use so links de quem voce confia.
