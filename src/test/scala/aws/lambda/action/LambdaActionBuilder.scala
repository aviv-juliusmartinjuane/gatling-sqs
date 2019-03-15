package aws.lambda.action

import aws.protocol.{AwsComponents, AwsProtocol}
import aws.lambda.LambdaCheck
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.protocol.ProtocolComponentsRegistry
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

case class LambdaActionBuilder(functionName: Expression[String], payload: Option[Expression[String]], checks: List[LambdaCheck]) extends ActionBuilder {

  private def components(protocolComponentsRegistry: ProtocolComponentsRegistry): AwsComponents =
    protocolComponentsRegistry.components(AwsProtocol.AwsProtocolKey)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._
    val statsEngine = coreComponents.statsEngine
    val awsComponents = components(protocolComponentsRegistry)
    LambdaAction(functionName, payload, checks, awsComponents.awsProtocol, coreComponents.actorSystem, statsEngine, next, coreComponents.clock)
  }

}
