package com.integracion.ReactSpring.Controllers

import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.saml2.provider.service.authentication.{OpenSaml4AuthenticationProvider, Saml2AuthenticatedPrincipal, Saml2Authentication}
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import scala.jdk.CollectionConverters._
import org.springframework.web.bind.annotation.RequestMapping

@Configuration
class OktaConfig {
  @Bean
  def configure(http: HttpSecurity): SecurityFilterChain = {
    val authenticationProvider = new OpenSaml4AuthenticationProvider
    authenticationProvider.setResponseAuthenticationConverter(groupConveter())

    http
      .authorizeHttpRequests(auth => auth
      .requestMatchers("/favicon.ico")
      .permitAll().anyRequest()
      .authenticated())
      .saml2Login(saml2 => saml2.authenticationManager(new ProviderManager(authenticationProvider)))
//      .saml2Logout(logout => Customizer.withDefaults())
      .build()
  }

  private def groupConveter(): Converter[OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication] = {
    val delegate = OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter

    responseToken => {
      val authentication = delegate.convert(responseToken)
      val principal = authentication.getPrincipal.asInstanceOf[Saml2AuthenticatedPrincipal]
      val groups = principal.getAttribute("groups")
      if (groups != null) {
        val authorities = groups.toArray.map(_.asInstanceOf[SimpleGrantedAuthority]).toSeq
        new Saml2Authentication(principal, authentication.getSaml2Response, authorities.asJava)
      } else {
        val authorities = authentication.getAuthorities;
        new Saml2Authentication(principal, authentication.getSaml2Response, authorities)
      }
    }
  }
}


@Controller
class HomeController {
  @RequestMapping(Array("/"))
  def home(@AuthenticationPrincipal principal: Saml2AuthenticatedPrincipal, model: Model): String = {
    model.addAttribute("name", principal.getName)
    model.addAttribute("emailAddress", principal.getFirstAttribute("email"))
    model.addAttribute("userAttributes", principal.getAttributes)
    "home"
  }
}
