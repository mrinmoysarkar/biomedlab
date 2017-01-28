//author: Mrinmoy Sarkar
//email: mrinmoy.pol@gmail.com

#include <SoftwareSerial.h>

int pulsePin = 3;           
volatile int BPM=0;                  
volatile int Signal=0;                
volatile int IBI = 600;             
volatile boolean Pulse = false; 
volatile boolean QS = false;  
volatile int rate[10];                  
volatile unsigned long sampleCounter = 0;   
volatile unsigned long lastBeatTime = 0;      
volatile int P =512;                
volatile int T = 512;               
volatile int thresh = 512;       
volatile int amp = 100;          
volatile boolean firstBeat = true;       
volatile boolean secondBeat = false;

int rx = 9;
int tx = 8;
SoftwareSerial bluetooth(rx, tx);


void setup() 
{
  //Serial.begin(57600);
  bluetooth.begin(57600);  
  interruptSetup();      
}

void loop()
{
  sendDataToProcessing('S', Signal); 
  if (QS == true)
  {                                     
    sendDataToProcessing('B',BPM); 
    sendDataToProcessing('Q',IBI);
    QS = false;                        
  }
  delay(20);
}

void sendDataToProcessing(char symbol, int data ){
  //Serial.print(symbol);    
  //Serial.println(data);
  bluetooth.print(symbol);    
  bluetooth.println(data);
  
}

void interruptSetup()
{
  TCCR2A = 0x02;
  TCCR2B = 0x05;
  OCR2A = 0x7C;
  TIMSK2 = 0x02;
  sei();
}

ISR(TIMER2_COMPA_vect)
{
  Signal = analogRead(pulsePin); 
  sampleCounter += 2;               
  int N = sampleCounter - lastBeatTime; 
  if(Signal < thresh && N > (IBI/5)*3)
  {
    if (Signal < T)
    { 
      T = Signal;    
    }
  }
  if(Signal > thresh && Signal > P)
  {  
    P = Signal;                     
  }
  if (N > 250)
  {
    if ( (Signal > thresh) && (Pulse == false) && (N > ((IBI/5)*3) ))
    {  
      Pulse = true;                                 
      IBI = sampleCounter - lastBeatTime;
      lastBeatTime = sampleCounter;    

      if(secondBeat)
      {              
        secondBeat = false;     
        for(int i=0; i<=9; i++)
        {   
          rate[i] = IBI;                      
        }
      }                       
      if(firstBeat)
      {                     
        firstBeat = false; 
        secondBeat = true;
        sei();            
        return;                          
      }
      word  runningTotal = 0;
      for(int i=0; i<=8; i++)
      {
        rate[i] = rate[i+1]; 
        runningTotal += rate[i];      
      }
      rate[9] = IBI;                   
      runningTotal += rate[9];             
      runningTotal /= 10;             
      BPM = 60000/runningTotal;
      QS = true;                        
    }             
  }
  if (Signal < thresh && Pulse == true)
  {  

    Pulse = false;                      
    amp = P - T;                        
    thresh = amp/2 + T;            
    P = thresh;                         
    T = thresh;
  }
  if (N > 2500)
  {    
    thresh = 512; 
    P = 512; 
    T = 512; 
    firstBeat = true;                 
    secondBeat = false;           
    lastBeatTime = sampleCounter;
  }
}







