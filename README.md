# RD_Challenge
## 2017 CISC Winter

### 팀명 : EPL</p>
### 소속 : 고려대학교</p>
### 이름 : 박진형, 이상훈, 주경호, 최원석</p>
### 파일설명</p>
* Packet_Analyzer : CAN IDS 프로그램
   Online/Offline 의 2가지 방식으로 동작
   Online 모드는 Packet_Injector로 부터 네트워크를 통해 패킷을 수신하여 동작함
   Offline 모드는 파일에 저장된 CAN Packet 데이터를 읽는 것을 패킷 수신으로 처리하여 동작함
   
   Execution Example
   
   cd [Packet_Analyzer root path]\bin
   java -cp .;..\lib\ChartDirector.jar emsec.korea.ui.StartFrame


* Packet_Injector : CAN Packet 주입 프로그램
  Packet Analyzer가 Online 모드일 때, Packet Analyzer에게 패킷을 송신함
  
  Execution Example
  
  cd [Packet_Injector root path]\bin
  java emsec.korea.Injector [Packet_Analyzer IP address] [Port Number] [full path of CAN DataSet file]
