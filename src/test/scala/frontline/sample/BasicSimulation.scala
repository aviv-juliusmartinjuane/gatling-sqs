package frontline.sample


import com.amazonaws.regions.Regions
import io.gatling.core.Predef._
import aws.Predef._
import aws.protocol.AwsProtocolBuilder
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicSessionCredentials}
import com.amazonaws.services.sqs.{AmazonSQS, AmazonSQSClientBuilder}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sqs.model.SendMessageRequest
import io.gatling.core.structure.ScenarioBuilder
import scala.concurrent.duration._
import scala.language.postfixOps





/**
 * Send message to AWS SQS FIFO queue in batch mode
 * max batchSize is 10
 */

class BasicSimulation extends Simulation {

  val devQueueUrl = "sqs_classified_events-republication-detection-dev"
  val endpoint = "https://sqs.eu-west-1.amazonaws.com"
  val accessKey = "key"
  val secretKey = "key"
  val session_token = "key"
  val batchSize = 10
  val messageBody: String = "{\n  \"Type\": \"Notification\",\n  \"MessageId\": \"57d0854f-e30c-4ad1-9a4e-f406b485181a\",\n  \"TopicArn\": \"arn:aws:sns:eu-west-1:220766614489:b2bmonolith-stage-ListingTopic\",\n  \"Message\": \"{\\\"idAnnonce\\\":381190783,\\\"idAgence\\\":240567,\\\"siSupprimer\\\":false,\\\"refAnnonce\\\":\\\"Demo boutique\\\",\\\"cp\\\":\\\"10552\\\",\\\"ville\\\":\\\"Athens\\\",\\\"adresse\\\":\\\"69-51 Kodrigktonos\\\",\\\"idPays\\\":250,\\\"libelleFr\\\":\\\"Chambre de service à vendre\\\",\\\"descriptifFr\\\":\\\"Description de l'annonce AvivImmo adapté pour apparaitre sur les fronts BD afin de faire du test endToend dans le cadre de la mise en place Caas Ingest au sein du groupe SeLoger\\\",\\\"idTypeBien\\\":1,\\\"idTypeTransaction\\\":2,\\\"dtCrea\\\":\\\"2023-03-06T08:05:00.000Z\\\",\\\"dtMaj\\\":\\\"2023-03-06T08:10:00.000Z\\\",\\\"refExterne\\\":\\\"60708-27180\\\",\\\"siExclusif\\\":false,\\\"source\\\":\\\"Import CaaS\\\",\\\"bQSiCensure\\\":false,\\\"siCoupDeCoeur\\\":false,\\\"dtArrivee\\\":\\\"2023-03-06T08:05:00.000Z\\\",\\\"transportStation\\\":\\\"metro 1\\\",\\\"idTypeTransport\\\":2,\\\"languesXml\\\":\\\"<en><descriptif>AvivImmo Description classified with description to do endToend test from CaasIngest to BD website for test group SeLoger integration and respect description critera</descriptif><libelle> APARTMENT SINGLEROOM</libelle><prox>Place note</prox></en>\\\",\\\"siHandicape\\\":true,\\\"idSousTypeBien\\\":49,\\\"siPromo\\\":false,\\\"genNbPhotos\\\":0,\\\"genNb360\\\":0,\\\"siCensureQualite\\\":false,\\\"indiceQualite\\\":0,\\\"indiceQualiteTri\\\":59,\\\"genNbVideos\\\":0,\\\"siEligibleBD\\\":false,\\\"raisonNonEligibleBD\\\":\\\"agence::belles_demeures\\\\r\\\\nType/Sous-type de bien non éligible au luxe.\\\\r\\\\n\\\",\\\"genNbPhotosAll\\\":0,\\\"genNb360All\\\":0,\\\"genNbVideosAll\\\":0,\\\"rankVisuel\\\":0,\\\"siWebVisiumMagic\\\":false,\\\"siIsmh\\\":false,\\\"idUniversBO\\\":1,\\\"latitude\\\":37.996147,\\\"longitude\\\":23.727851,\\\"siGeolocEchec\\\":false,\\\"statutPCSLN\\\":64,\\\"idTypeCensureWi\\\":0,\\\"siBrouillon\\\":false,\\\"siHonorairesAcquereur\\\":true,\\\"siCopropriete\\\":true,\\\"nbLotsCopropriete\\\":10,\\\"siProcedureSyndicat\\\":false,\\\"descriptionProcedureSyndicat\\\":\\\"fin de la procédure prévue à la fin de l'année\\\",\\\"siAnnonceLocataire\\\":false,\\\"siParticulier\\\":false,\\\"liveVisitState\\\":1,\\\"idStatutGeoloc\\\":7,\\\"agence\\\":{\\\"idClientRcu\\\":\\\"RC-369450\\\"},\\\"publications\\\":[1],\\\"vente\\\":{\\\"px\\\":800000.5,\\\"siPrixHt\\\":false,\\\"idTypeResponsableHonoraires\\\":1},\\\"appartement\\\":{\\\"anneeConstruct\\\":\\\"1960\\\",\\\"bilanConsoEnergie\\\":\\\"A\\\",\\\"bilanEmissionGES\\\":\\\"A\\\",\\\"consoEnergie\\\":1,\\\"emissionGES\\\":1,\\\"estimationMaxCoutDPE\\\":100.1,\\\"estimationMinCoutDPE\\\":100.2,\\\"idTypeChauffage\\\":128,\\\"idTypeCuisine\\\":1,\\\"nbBalcons\\\":2,\\\"nbBoxes\\\":1,\\\"nbChambres\\\":10,\\\"nbEtages\\\":5,\\\"nbParkings\\\":15,\\\"nbPieces\\\":13,\\\"nbSallesDeBain\\\":2,\\\"nbTerrasses\\\":10,\\\"nbToilettes\\\":2,\\\"refVersionDpe\\\":2,\\\"siAlarme\\\":false,\\\"siAscenseur\\\":false,\\\"siCableTv\\\":true,\\\"siCalme\\\":false,\\\"siCave\\\":false,\\\"siCheminee\\\":false,\\\"siDpeNonRenseigne\\\":false,\\\"siDuplex\\\":false,\\\"siEst\\\":false,\\\"siJardin\\\":true,\\\"siLotNeuf\\\":false,\\\"siMeuble\\\":true,\\\"siNeuf\\\":false,\\\"siNord\\\":false,\\\"siOuest\\\":true,\\\"siParquet\\\":true,\\\"siPiscine\\\":true,\\\"siPlacards\\\":false,\\\"siPrixParkingsInclus\\\":false,\\\"siRefaitNeuf\\\":false,\\\"siSejour\\\":true,\\\"siSud\\\":false,\\\"siTerrasse\\\":true,\\\"siToilettesSeparees\\\":false,\\\"siTravaux\\\":false,\\\"siVisavis\\\":false,\\\"siVue\\\":false,\\\"surfBalcons\\\":8,\\\"surfCave\\\":5.5,\\\"surfJardin\\\":130.5,\\\"surfSejour\\\":20.5,\\\"surfTerrasses\\\":7.5,\\\"surface\\\":40.5}}\",\n  \"Timestamp\": \"2023-05-02T09:47:01.924Z\",\n  \"SignatureVersion\": \"1\",\n  \"Signature\": \"0/YhHsaUI0Lcrs0n6QAxy/htislxBOOLRyLxvDmZMfiCyvclqFp5RDuLBEvNEtmF4jZGQEnxxzmkvEza3jt+3fA553SHpKfgOoIdGVt45fCkJ/XYC199PC189LeTf696ObdgtKbEFrYZJOGha+qSH2gxGeRJ8NiiFZrppcxndAIUgG1aHBestgni18Jr+lPc/NSUQHsXGHBduxynZOdhQIc2mVkco+wfoLGSImaOMblWoBf7OBZGpP8LbxtGAc7f4q7ZHPr5MMVS6O8g7MdGkERd4sMfYFkwqmT5Yp7Ia0vj+Ef/P3RcctYNRDwe5XbJAQMCQrXrqvP+8fC6P1FDsQ==\",\n  \"SigningCertURL\": \"https://sns.eu-west-1.amazonaws.com/SimpleNotificationService-56e67fcb41f6fec09b0196692625d385.pem\",\n  \"UnsubscribeURL\": \"https://sns.eu-west-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-1:220766614489:b2bmonolith-stage-ListingTopic:2df0495e-ea6a-4903-9231-a8039d8bf7a6\",\n  \"MessageAttributes\": {\n    \"correlationId\": {\n      \"Type\": \"String\",\n      \"Value\": \"00-7fb795186408cd469f983bf1d322975e-00b36e9badda71ff-00\"\n    },\n    \"state\": { \"Type\": \"String\", \"Value\": \"CREATED\" }\n  }\n}"
  val awsCreds = new BasicSessionCredentials(accessKey, secretKey, session_token)
  var client: AmazonSQS = AmazonSQSClientBuilder
    .standard()
    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    .withEndpointConfiguration(new EndpointConfiguration(endpoint, Regions.EU_WEST_1.getName)) // update region if needed sqs fifo queue
    .build()

  val awsConfig: AwsProtocolBuilder = Aws
    .batchSize(batchSize)
    .queueUrl(s"$endpoint/$devQueueUrl")
    .awsQueue(client)

  val scn: ScenarioBuilder = scenario("SQS Perf Test")
    .exec(session => {
      val request = new SendMessageRequest(devQueueUrl, messageBody)
      client.sendMessage(request)
      session
    }
    )

  setUp(
    scn.inject(
      //atOnceUsers(2)
      constantUsersPerSec (100) during (60 seconds) //max of 15300 classifieds per hour
    ) // 1M
    //constantUsersPerSec (300) during (333 seconds) // 1M
    //              constantUsersPerSec(300) during (160 seconds) // 0.5M
    //constantUsersPerSec (100) during (10 seconds) // 10K
    // other example: Let's have 10 regular users and 2 admins, and ramp them over 10 seconds so we don't hammer
    // the server
    //users.inject(rampUsers(10).during(10)), admins.inject(rampUsers(2).during(10))
    //).protocols(jmsProtocol)
  ).protocols()

  after {
    client.shutdown()
  }



}
