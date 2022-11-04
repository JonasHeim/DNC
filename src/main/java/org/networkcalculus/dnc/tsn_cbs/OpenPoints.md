# Open Points 25.10.2022
* Input sanitation\
Parameter verifizieren, etc. ...
* **Aperiodische flows** \
Verdopplung von b oder m?
* **IdleSlope eines flows**\
Wenn ein Flow auf einem Pfad zu einem ServerGraph mit einer idleSlope hinzugefügt wird, bleibt die idleSlope für jeden Server auf dem Pfad konstant?
* Kann die **min Idle Slope** eines flows direkt aus seiner TSpec ermittelt werden\
Mit idSlp = (MIF*MFS)/CMI, bsp.:
```
MIF=1
MFS=512 Bit (min. size)
CMI=1ms

idSlp = (1 * 512 Bit)/1ms = 512kBit/s
```
IdleSlope konstant für Server bzw. Queue. Beim Hinzufügen des Servers mitgeben.
Bei Adden von Flow muss verifiziert werden, ob Idle Slope noch valide!

* **Max. Credit**\
Wenn wir die max. Paketgröße für BE-Traffic konstant auf 1500Byte = 12kBit setzen, dann ist auch l^(u,v,p) in allem max. Credit Berechnungen konstant 12kBit, unabhängig von der anderen Queues?
* Interpretation **maxCredit**\
    Die maximale Anzahl an Credits/Bits die mit der IdleSlope in der Zeit, die eine Queue im WC-Fall warten muss, angesammelt werden können.\
    Der WC Fall ist wenn gerade ein BE-Traffic Paket auf den Link gegeben wird und! alle höherprioren Queues zeitgleich sendebereit sind also nach dem BE Paket jeweils ihren gesamten min. Credit ausnutzen.
* **Max. frame length**\
    Ethernet Interpacket gap (12 Byte) und Präambel (7 Byte) mit einberechnen (gesamt 19 Byte)?
    Aus min. Länge 64 Byte -> 83 Byte\
    Aus max. Länge 1500 Byte -> 1519 Byte\
Ja, mit dazu nehmen
* Hinzufügen **mehrerer Flows gleicher Priorität**\
Wenn bereits ein Flow im ServerGraph existiert und ein zweiter mit identischem Pfad und Priorität hinzugefügt wird, wie verhalten sich die CBS-Queues der Serves des Pfades?
Werden nur die idleSlopes addiert und anschließend der min- und max-Credit, CBS-ShapingCurve und ServiceCurve neu berechnet?\
* Wenn ein hoch-priorer Flow zu ServerGraph hinzugefügt wird, müssen die Parameter (credits, shaper, sc, aggr. ac) für alle Flows mit niedrigerer Prio für alle Server der Pfade neu berechnet werden.\
**Voraussetzen**: Flows in steigender Priorität zum ServerGraph hinzufügen\
**Nochmal überdenken..., evtl. erst in TFA alles berechnen**
* CBS Server können auch Talker, Switches oder Listener sein\
-> Variable aus ENUM\
Jede Queue in Server braucht Referenz auf Output Link (inkl. Capacity)
* **Vergleich** CBS shaped networks <-> konventionelle networks\
In CBS wird ja jede Priorität als eigenes Netzwerk angesehen, aber ein gesamtes konvent. Netzwerk. 
- **Vergleich**: Arrival Curves CBS <-> konventionell\
Einfach die gleiche AC der TSN flows für konv. Netzwerk nehmen?
- **Vergleich**\
Service Curves der Server. In CBS existiert ja pro CBS Queue eine eigene ServiceCurve. Einfach addieren?\

* Für Vergleich 3 Schritte:
1. TFA ohne shaping
2. TFA nur mit link shaping
3. TFA mit CBS und link shaping

* Ich kann mir aussuchen, ob Talker shaping anwenden (aktuell Talker nur link shaping)
* **Schreiben, Schreiben, Schreiben...**

# Open Points 03.11.2022
* **Konstanter Faktor** von 42 Bytes auf Payload addiert (angenommen: 802.1Q getaggte Frames wegen der Prioritäten).
* **IdleSlopes** (und damit auch SendSlopes) werden jetzt **konstant pro Server** festgelegt.\
Beim Hinzufügen eines Flows wird damit der min./max. Credit der Queue aktualisiert. 
* Für Berechnung lmax = BE_max oder Flow_max\
Bei c_max -> BE_max\
Bei c_min -> Flow_max

* Für aperiodische Flows sagt Azua2014 [S. 5] b' = 2*b
* Die Reihenfolge beim Hinzufügen der Flows muss in **absteigender Priorität** geschehen, da die Latency der
ServiceCurves der Queues von der max. Credit der Queue abhängig ist und sich diese ändert, sobald ein hochpriorer Flow
zum Server/Output Link hinzugefügt wird. Dadurch verändert sich auch der Output Bound und entsprechend alle weiteren
ArrivalCurves auf dem Pfad.
* Soll Best Effort traffic modelliert werden?\
Nein, es wird einfach angenommen, dass er vorhanden ist.
* Für die Modellierung bzw. den **Vergleich** mit konventionellem Netzwerk **ohne Shaping** (kein CBS, kein Link): Wie müssen
die **ServiceCurves** modelliert werden? Gibt ja keine Credits bzw. IdleSlopes?