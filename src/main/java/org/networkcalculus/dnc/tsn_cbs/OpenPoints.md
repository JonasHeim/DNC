# Open Points
* Aperiodische flows \
Verdopplung von b oder m?
* IdleSlope eines flows\
Wenn ein Flow auf einem Pfad zu einem ServerGraph mit einer idleSlope hinzugefügt wird, bleibt die idleSlope für jeden Server auf dem Pfad konstant?
* Kann die Idle Slope eines flows direkt aus seiner AC ermittelt werden mit idSlp = (MIF*MFS)/CMI
* Max. Credit\
Wenn wir die max. Paketgröße für BE-Traffic konstant auf 1500Byte = 12kBit setzen, dann ist auch l^(u,v,p) in allem max. Credit Berechnungen konstant 12kBit, unabhängig von der anderen Queues?
* Hinzufügen mehererer Flows gleicher Priorität\
Wenn bereits ein Flow im ServerGraph existiert und ein zweiter mit identischem Pfad und Priorität hinzugefügt wird, wie verhalten sich die CBS-Queues der Serves des Pfades?
Werden nur die idleSlopes addiert und anschließend der min- und max-Credit, CBS-ShapingCurve und ServiceCurve neu berechnet?
* Wenn ein Flow zu ServerGraph hinzugefügt wird, kann gleich die AggregatedArrivalCurve für alle Server auf Pad neu berechnet werden?

* CBS Server können auch Talker, Switches oder Listener sein -> Variable aus ENUM
EVery Queue in Server braucht Referenz auf Output Link (inkl. Capacity)