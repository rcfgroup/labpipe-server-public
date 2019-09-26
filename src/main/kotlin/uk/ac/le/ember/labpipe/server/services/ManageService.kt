package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import io.javalin.http.Context
import org.apache.commons.lang3.RandomStringUtils
import org.litote.kmongo.*
import org.mindrot.jbcrypt.BCrypt
import org.simplejavamail.email.Recipient
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.EmailTemplates
import uk.ac.le.ember.labpipe.server.data.AccessToken
import uk.ac.le.ember.labpipe.server.data.Operator
import uk.ac.le.ember.labpipe.server.notification.EmailUtil
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.util.*

object ManageService {
    private fun createOperator(ctx: Context): Context {
        val email = ctx.queryParam("email")
        val name = ctx.queryParam("name")
        email?.run {
            name?.run {
                var currentOperator =
                    Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS)
                        .findOne(Operator::email eq email)
                if (currentOperator != null) {
                    ctx.status(400)
                    return ctx.result("Operator with email [$email] already exists.")
                } else {
                    var operator = Operator(email = email)
                    operator.name = name
                    operator.username = email
                    val tempPassword = RandomStringUtils.randomAlphanumeric(8)
                    operator.passwordHash = BCrypt.hashpw(tempPassword, BCrypt.gensalt())
                    operator.active = true
                    Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS).insertOne(operator)
                    EmailUtil.sendEmail(
                        from = Recipient(
                            Runtime.config.notificationEmailName,
                            Runtime.config.notificationEmailAddress,
                            null
                        ),
                        to = listOf(
                            Recipient(
                                operator.name,
                                operator.email,
                                null
                            )
                        ),
                        subject = "Your LabPipe Operator Account",
                        text = String.format(EmailTemplates.CREATE_OPERATOR_TEXT, operator.name, operator.email, tempPassword),
                        html = String.format(EmailTemplates.CREATE_OPERATOR_HTML, operator.name, operator.email, tempPassword),
                        async = true
                    )
                    ctx.status(200)
                    return ctx.result(Constants.MESSAGES.OPERATOR_CREATED)
                }
            }
        }
        ctx.status(400)
        return ctx.result("Please make sure you have provided name and email.")
    }

    private fun createToken(ctx: Context): Context {
        val operator = AuthManager.getUser(ctx)
        operator?.run {
            var token = UUID.randomUUID().toString()
            while (Runtime.mongoDatabase.getCollection<AccessToken>(Constants.MONGO.REQUIRED_COLLECTIONS.ACCESS_TOKENS)
                    .findOne(AccessToken::token eq token) != null) {
                token = UUID.randomUUID().toString()
            }
            var key = RandomStringUtils.randomAlphanumeric(16)
            var accessToken = AccessToken(token = token, keyHash = BCrypt.hashpw(key, BCrypt.gensalt()))
            Runtime.mongoDatabase.getCollection<AccessToken>(Constants.MONGO.REQUIRED_COLLECTIONS.ACCESS_TOKENS).insertOne(accessToken)
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.config.notificationEmailName,
                    Runtime.config.notificationEmailAddress,
                    null
                ),
                to = listOf(
                    Recipient(
                        operator.name,
                        operator.email,
                        null
                    )
                ),
                subject = "Your LabPipe Operator Account",
                text = String.format(EmailTemplates.CREATE_TOKEN_TEXT, accessToken, key),
                html = String.format(EmailTemplates.CREATE_TOKEN_HTML, accessToken, key),
                async = true
            )
            ctx.status(200)
            return ctx.result(Constants.MESSAGES.TOKEN_CREATED)
        }
        ctx.status(401)
        return ctx.result(Constants.MESSAGES.UNAUTHORIZED)
    }

    fun routes() {
        println("Add parameter service routes.")
        Runtime.server.get(
            Constants.API.MANAGE.CREATE.OPERATOR, { ctx -> createOperator(ctx) },
            roles(AuthManager.ApiRole.AUTHORISED)
        )
        Runtime.server.get(
            Constants.API.MANAGE.CREATE.TOKEN, { ctx -> createToken(ctx) },
            roles(AuthManager.ApiRole.AUTHORISED)
        )
    }
}