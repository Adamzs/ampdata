/*
 * Provides a unified view of both air and sea arrival manifests.
 */
CREATE OR replace VIEW `AirSeaManifest` AS
   (select FedTime,ArrivalTime,CargoId,NumItems,Amount,CargoType,RLN,VirtualRlnId,MissionId,MissionNum,AssetId,FromPurpose,ToPurpose,FromLocationType,ToLocationType,DynamicPOD,OnloadGeoloc,OffloadGeoloc from AirArrivalManifest)
 UNION
   (select FedTime,ArrivalTime,CargoId,NumItems,Amount,CargoType,RLN,VirtualRlnId,MissionId,MissionNum,AssetId,FromPurpose,ToPurpose,FromLocationType,ToLocationType,DynamicPOD,OnloadGeoloc,OffloadGeoloc from SeaArrivalManifests)
ORDER BY CargoId;
 