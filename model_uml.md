```plantuml
title Model class Diagram

class BloodAnalysisSession{
    + BloodAnalysisSession()
    + process(image : Image, instant : Instant) : boolean
    ....
    + getFramesInfo() : FrameInfo[]
    + getFramesInfo(duration : Duration) : FrameInfo[]
    ....
    + getDuration() : Duration
    + getHeartbeat() : int
    + getHeartbeat(duration : Duration) : int
    ....
    + {static} loadSession(TODO) : BloodAnalysisSession
    + saveSession(TODO) : boolean
}
BloodAnalysisSession *-- FrameInfo : mFramesInfo : 1..*

class FrameInfo{
    - mInstant : Instant
    - mRedMean : int
    ----
    + FrameInfo(instant : Instant)
    + fillInfo(image : Image) : boolean
    ....
    - setRedMean(redMean : int)
    - setRedMean(image : Image)
}
```
