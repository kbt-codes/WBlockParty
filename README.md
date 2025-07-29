# 🎮 WBlockParty - Plugin de BlockParty para Minecraft

Um plugin elegante e robusto de BlockParty para servidores Minecraft Paper 1.21.4+.

## ✨ Características

- 🎯 **Sistema de Partidas Robusto**: Validações completas e tratamento de erros
- 🎨 **Configuração Modular**: Arquivos separados para diferentes aspectos
- 🎵 **Sistema de Áudio Avançado**: Sons configuráveis e música de discos
- 📊 **Scoreboard Dinâmico**: Informações em tempo real
- 🏆 **Sistema de Estatísticas**: Contagem de vitórias e jogos
- ⚡ **Performance Otimizada**: Thread-safe e cache inteligente
- 🔧 **Configuração Flexível**: Múltiplos arquivos de configuração

## 📁 Estrutura de Arquivos

```
WBlockParty/
├── config.yml          # Configurações principais
├── messages.yml        # Todas as mensagens do plugin
├── sounds.yml          # Configurações de sons e música
├── scoreboard.yml      # Configurações do scoreboard
├── arena.yml           # Configurações da arena
└── data.yml           # Dados do plugin (NÃO MODIFIQUE!)
```

## ⚙️ Configuração

### 📋 config.yml (Configurações Principais)

```yaml
# Configurações básicas do jogo
game:
  min-players: 2
  max-players: 20
  lobby-timer: 30
  round-base-time: 30
  round-min-time: 10
  max-game-time: 600
  game-end-delay: 3
  fall-check-interval: 10

# Configurações de debug
debug:
  enabled: false
  level: 1

# Configurações de performance
performance:
  auto-save-interval: 300
  auto-cleanup: true
  cleanup-days: 30

# Configurações de permissões
permissions:
  basic: "blockparty.basic"
  admin: "blockparty.admin"
  config: "blockparty.config"
  bypass-min: "blockparty.bypass.min"
  bypass-max: "blockparty.bypass.max"

# Configurações de arquivos
files:
  separate-files: true
  messages: "messages.yml"
  sounds: "sounds.yml"
  scoreboard: "scoreboard.yml"
  arena: "arena.yml"
  data: "data.yml"
```

### 💬 messages.yml (Mensagens)

```yaml
# Prefixo das mensagens
prefix: "§6[BlockParty] §r"

# Cores das mensagens
colors:
  success: "§a"
  error: "§c"
  info: "§e"
  warning: "§6"

# Mensagens do jogo
game:
  start: "§aPartida iniciada!"
  join: "§a{player} entrou no BlockParty! ({current}/{max})"
  winner: "§6🏆 {player} VENCEU O BLOCKPARTY! 🏆"
  eliminated: "§cVocê caiu! Você foi eliminado!"

# Mensagens de título
title:
  victory: "§6🏆 VITÓRIA! 🏆"
  victory-subtitle: "§aVocê venceu o BlockParty!"
  block-target: "§6§l{block}"
  block-subtitle: "§eFique neste bloco para sobreviver!"
```

### 🎵 sounds.yml (Sons e Música)

```yaml
# Configurações gerais
enabled: true
volume: 1.0
pitch: 1.0

# Sons de jogadores
players:
  join: "ENTITY_PLAYER_LEVELUP"
  leave: "ENTITY_PLAYER_LEVELUP"
  eliminated: "ENTITY_PLAYER_DEATH"
  victory: "ENTITY_PLAYER_LEVELUP"

# Sons de jogo
game:
  start: "ENTITY_PLAYER_LEVELUP"
  end: "ENTITY_PLAYER_LEVELUP"
  block-show: "BLOCK_NOTE_BLOCK_PLING"
  block-remove: "BLOCK_GLASS_BREAK"

# Configurações de música
music:
  enabled: true
  volume: 0.5
  pitch: 1.0
  available-discs:
    - "MUSIC_DISC_CAT"
    - "MUSIC_DISC_BLOCKS"
    - "MUSIC_DISC_CHIRP"
    # ... mais discos
```

### 📊 scoreboard.yml (Scoreboard)

```yaml
# Configurações gerais
enabled: true
update-interval: 20

# Scoreboard do lobby
lobby:
  title: "§6§lBLOCKPARTY"
  lines:
    - "§7-----------------"
    - "§fJogadores Online"
    - "§a{players}§7/§c{max}"
    - "§e"
    - "§fMínimo para Iniciar"
    - "§a{min} jogadores"
    - "§e"
    - "§fStatus"
    - "§aAguardando..."
    - "§7-----------------"

# Scoreboard da partida
game:
  title: "§6§lBLOCKPARTY"
  lines:
    - "§7-----------------"
    - "§fRodada: §a{round}"
    - "§fBloco: {block}"
    - "§fTempo: §a{time}s"
    - "§fVivos: §a{players}"
    - "§fTempo Total: §a{total-time}"
    - "§7-----------------"
```

### 🏟️ arena.yml (Arena)

```yaml
# Configurações gerais
floor-pattern: RANDOM
arena-size: 37
arena-height: 1

# Blocos disponíveis
available-blocks:
  - "WHITE_TERRACOTTA"
  - "RED_TERRACOTTA"
  - "BLUE_TERRACOTTA"
  - "GREEN_TERRACOTTA"
  - "YELLOW_TERRACOTTA"
  - "PURPLE_TERRACOTTA"
  - "ORANGE_TERRACOTTA"
  - "PINK_TERRACOTTA"
  - "LIME_TERRACOTTA"

# Configurações de segurança
safety:
  min-edge-distance: 2
  min-arena-height: 1
  max-arena-height: 10
  check-world-bounds: true
  check-solid-ground: true
  check-player-space: true

# Configurações de performance
performance:
  optimize-rendering: true
  use-chunks: true
  chunk-size: 16
  cache-blocks: true
  cache-time: 100
```

### ⚠️ data.yml (Dados - NÃO MODIFIQUE!)

```yaml
# ⚠️  AVISO IMPORTANTE  ⚠️
# 
# ESTE ARQUIVO CONTÉM DADOS DO PLUGIN
# 
# ⚠️  NÃO MODIFIQUE ESTE ARQUIVO MANUALMENTE!  ⚠️
# 
# Modificações manuais podem:
# - Corromper os dados do plugin
# - Causar erros no funcionamento
# - Perder informações dos jogadores
# - Quebrar o plugin completamente
# 
# Se você precisa alterar configurações, use:
# - config.yml (configurações gerais)
# - messages.yml (mensagens)
# - sounds.yml (sons)
# - scoreboard.yml (scoreboard)
# - arena.yml (arena)
```

## 🎮 Comandos

| Comando | Descrição | Permissão |
|---------|-----------|-----------|
| `/blockparty join` | Entrar no jogo | `blockparty.basic` |
| `/blockparty leave` | Sair do jogo | `blockparty.basic` |
| `/blockparty start` | Iniciar partida | `blockparty.admin` |
| `/blockparty stop` | Parar partida | `blockparty.admin` |
| `/blockparty setlobby` | Configurar lobby | `blockparty.admin` |
| `/blockparty setarena` | Configurar arena | `blockparty.admin` |
| `/blockparty setspectator` | Configurar espectador | `blockparty.admin` |
| `/blockparty setsign` | Configurar placa | `blockparty.admin` |
| `/blockparty help` | Mostrar ajuda | `blockparty.basic` |

## 🔧 Instalação

1. **Baixe o plugin**: `WBlockParty-3.3.jar`
2. **Coloque no servidor**: `plugins/` folder
3. **Reinicie o servidor**
4. **Configure os arquivos**: Edite os arquivos YAML conforme necessário
5. **Configure as localizações**: Use os comandos de configuração

## 🎯 Funcionalidades Avançadas

### 🎮 Sistema de Partidas Robusto

- ✅ **Validações Completas**: Verificações de estado e jogadores
- ✅ **Thread Safety**: Uso de ConcurrentHashMap para segurança
- ✅ **Tratamento de Erros**: Try-catch em todas as operações críticas
- ✅ **Sistema de Pausa**: `pauseGame()` e `resumeGame()`
- ✅ **Timer de Jogo**: Controle de tempo máximo de partida
- ✅ **Fall Check Task**: Verificação contínua de jogadores caídos

### 📊 Sistema de Estatísticas

- ✅ **Contagem de Vitórias**: `getPlayerWins(player)`
- ✅ **Estatísticas Globais**: `getTotalGames()` e `getTotalPlayers()`
- ✅ **Tempo de Jogo**: `getGameTime()` para acompanhar duração
- ✅ **Persistência**: Dados mantidos durante sessão

### 🎵 Sistema de Áudio Avançado

- ✅ **Sons Configuráveis**: Todos os sons personalizáveis
- ✅ **Volume e Pitch**: Configuráveis individualmente
- ✅ **Música de Discos**: Sistema completo de música
- ✅ **Sons 3D**: Posição do jogador considerada
- ✅ **Fade e Crossfade**: Efeitos de transição

### 🔧 Configuração Modular

- ✅ **Arquivos Separados**: Organização por funcionalidade
- ✅ **Cache Inteligente**: Performance otimizada
- ✅ **Fallbacks**: Configurações padrão seguras
- ✅ **Validação**: Verificação de integridade
- ✅ **Backup Automático**: Proteção contra perda de dados

## 🚀 Performance

- **Thread Safety**: Uso de estruturas thread-safe
- **Cache Inteligente**: Redução de I/O desnecessário
- **Tasks Otimizadas**: Cancelamento adequado de tasks
- **Memory Management**: Limpeza automática de dados antigos
- **Configuração Lazy**: Carregamento sob demanda

## 🛠️ Desenvolvimento

### Estrutura do Projeto

```
src/main/java/me/willowdev/blockparty/
├── WBlockParty.java              # Classe principal
├── GameManager.java              # Gerenciador de jogo
├── command/                      # Comandos
│   ├── BlockPartyCommand.java
│   ├── BlockPartyJoinCommand.java
│   ├── BlockPartyLeaveCommand.java
│   ├── BlockPartySetArenaCommand.java
│   ├── BlockPartySetLobbyCommand.java
│   ├── BlockPartySetSignCommand.java
│   ├── BlockPartySetSpectatorCommand.java
│   ├── BlockPartyStartCommand.java
│   ├── BlockPartyStopCommand.java
│   └── BlockPartyTabCompleter.java
├── listener/                     # Listeners
│   └── SignClickListener.java
└── utils/                       # Utilitários
    ├── ConfigManager.java        # Gerenciador de configuração
    └── MessageUtils.java         # Utilitários de mensagem
```

### Compilação

```bash
mvn clean compile
mvn clean package
```

## 📝 Changelog

### v3.3 - Sistema de Configuração Modular
- ✅ **Configuração Dividida**: Arquivos separados por funcionalidade
- ✅ **Sistema Robusto**: Validações e tratamento de erros
- ✅ **Thread Safety**: Uso de estruturas thread-safe
- ✅ **Cache Inteligente**: Performance otimizada
- ✅ **Sistema de Estatísticas**: Contagem de vitórias e jogos
- ✅ **Áudio Avançado**: Sons configuráveis e música
- ✅ **Proteção de Dados**: Arquivo data.yml com avisos
- ✅ **Documentação Completa**: README atualizado

### v3.2 - Melhorias de Performance
- ✅ Otimização de tasks
- ✅ Sistema de cache
- ✅ Limpeza automática

### v3.1 - Funcionalidades Básicas
- ✅ Sistema de partidas
- ✅ Comandos básicos
- ✅ Scoreboard
- ✅ Sons e música

## 🤝 Suporte

- **Issues**: Reporte bugs no GitHub
- **Documentação**: Consulte este README
- **Configuração**: Use os arquivos YAML para personalizar

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.

---

**Desenvolvido com ❤️ para a comunidade Minecraft** 