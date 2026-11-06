package com.tienda.config;

import com.tienda.cola.ColaPedidos;
import com.tienda.cola.ResultadosPendientes;
import com.tienda.recurso.Almacen;
import com.tienda.server.RegistroClientes;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans del nucleo de la tienda: cola, almacen, futures y aforo de
 * peticiones, todos parametrizados por {@link TiendaProperties}.
 */
@Configuration
@EnableConfigurationProperties(TiendaProperties.class)
public class TiendaConfig {

    @Bean
    public ColaPedidos colaPedidos(TiendaProperties propiedades) {
        return new ColaPedidos(propiedades.getCapacidadCola());
    }

    @Bean
    public Almacen almacen(TiendaProperties propiedades) {
        return new Almacen("Almacen", propiedades.getCapacidadAlmacen());
    }

    @Bean
    public ResultadosPendientes resultadosPendientes() {
        return new ResultadosPendientes();
    }

    @Bean
    public RegistroClientes registroClientes(TiendaProperties propiedades) {
        return new RegistroClientes(propiedades.getMaxPeticionesSimultaneas());
    }
}
