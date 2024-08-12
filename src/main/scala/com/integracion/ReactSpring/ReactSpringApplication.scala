package com.integracion.ReactSpring

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class ReactSpringApplication {
  def run(args: String*): Unit = {
    SpringApplication.run(classOf[ReactSpringApplication])
  }
}
object ReactSpringApplication extends App {
  SpringApplication.run(classOf[ReactSpringApplication])
}