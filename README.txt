# SDIS-Project1

Turma 2 Grupo 2

Authors:
Diogo Miguel Sousa Barroso
João Pedro Milano Silva Cardoso

Compilação:
 - A aplicação não utiliza bibliotecas exteriores, é compilada como uma aplicação normal em Java.

Pode ser necessário mudar informação no ficheiro de configuração devido ao caminho de vários ficheiros.
Este ficheiro tem de estar acessível à aplicação.

O ficheiro config.properties contém a seguinte informação:
 - backup_path -> Localização onde os chunks vão ser guardados.
 - restore_path -> Localização onde os chunks vão ser temporariamente guardados para a restauração de um ficheiro.
 - fileRestore_path -> Localização onde o ficheiro restaurado vai ser guardado.
 - file_path -> Ficheiro de texto que guarda informação sobre os ficheiros.
 - chunk_path -> Ficheiro de texto que guarda informação sobre os chunks.
 - mc_ip -> IP do Multicast Control Channel
 - mc_port -> Port do Multicast Control Channel
 - mdb_ip -> IP do Multicast Backup Channel
 - mdb_port -> Port do Multicast Backup Channel
 - mdr_ip -> IP do Multicast Restore Channel
 - mdr_port -> Port do Multicast Restore Channel
 - totalSpace -> Espaço total disponível para a aplicação 
 - usedSpace -> Espaço utilizado pela aplicação.
 
Correr o programa:
 - Utilizando o Windows CMD com directório actual sendo a pasta bin do projeto, por exemplo:
	java Main.Peer <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT> 
	java Main.Peer 224.0.1.1 4555 224.0.1.2 4556 224.0.1.3 4557

 - O projeto pode também ser executado sem argumentos. Nesse caso carrega a informação sobre IPs e Ports do ficheiro de configuração.
 
Utilização:
 Depois de correr o projeto, este fica à espera de comandos.
 
 BACKUP:
	backup <File Path> <Replication Degree>
	backup imagem.jpg 1
	
 RESTORE:
	restore <File Path>
	restore imagem.jpg
	
 DELETE:
	delete <File Path>
	delete imagem.jpg
	
 RECLAIM SPACE:
	reclaim
	
 QUIT
	quit -> Ao fazer 'quit' são atualizadas as listas de ficheiros, fileList.txt e chunks, chunkList.txt, bem como o espaço usado.
	
  
