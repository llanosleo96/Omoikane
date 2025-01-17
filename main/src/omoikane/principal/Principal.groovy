/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package omoikane.principal

import omoikane.sistema.*
import omoikane.sistema.Usuarios as SisUsuarios

import omoikane.sistema.cortes.ContextoCorte
import org.apache.log4j.Logger
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.context.ApplicationContext
import omoikane.exceptions.UEHandler
import omoikane.sistema.huellas.ContextoFPSDK.SDK
import omoikane.sistema.huellas.HuellasCache
import omoikane.sistema.seguridad.AuthContext

/**
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 *  * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 *  * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 *  * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 *  * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 *  * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 *  * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 *  * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 *
 * 
 *  * @author Octavio
 */
public class Principal {
        static Escritorio escritorio
        static MenuPrincipal        menuPrincipal;
        static def        config
        public static int                   IDAlmacen
        public static int                   IDCaja
        public static int                   sysAncho
        public static int                   sysAlto
        public static int                   CacheSTableAtras
        public static int                   CacheSTableAdelante
        public static boolean               fondoBlur
        public static String                puertoImpresion
        public static boolean               impresoraActiva
        public static boolean               scannerActivo
        public static boolean               basculaActiva
        public static HashMap               driverBascula
        public static String                URLMySQL
        public static String                loginJasper
        public static String                passJasper
        public static int                   scannerBaudRate
        public static String                scannerPort
        public static SDK                   sdkFingerprint = SDK.ONETOUCH;
        public static ShutdownHandler       shutdownHandler
        public static def                   toFinalizeTracker       = [:]
        public static def                   scanMan
        public static def                   tipoCorte               = ContextoCorte.TIPO_DUAL
        final  static def                   ASEGURADO               = true
        final  static def                   SHOW_UNHANDLED_EXCEPTIONS = true
        public static Logger                logger                  = Logger.getLogger(Principal.class);
        public static ApplicationContext    applicationContext;
        public static final Boolean         DEBUG = false;
        public static final String          VERSION = "1.4.0 RC 2";
        public static def                   authType                = AuthContext.AuthType.NIP;

    public static void main(args)
        {
            logger.trace( "Prueba de codificación: áéíóú" )
            iniciar()
        }
        public static ApplicationContext getContext() {
            return applicationContext;
        }
        static iniciar()
        {
            try {

            logger.trace("Iniciando sistema. Versión " + VERSION);
            configExceptions()
            def splash = new Splash()
            splash.iniciar()
            Locale.setDefault(Locale.US);

            shutdownHandler = new ShutdownHandler()
            Runtime.getRuntime().addShutdownHook(shutdownHandler);

            splash.setText("Cargando configuración...")
            config = new omoikane.sistema.Config()

            splash.setText("Cargando ApplicationContext...")
            applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");

            splash.setText("Cargando huellas en caché...")
            applicationContext.getBean(HuellasCache.class).getHuellasBD();

            splash.setText("Inicializando escritorio...")
            //Herramientas.utilImpresionCortes()
            //System.exit(0)
            escritorio = new Escritorio()
            escritorio.iniciar()
            println "iniciando menús"
            splash.setText("Inicializando menú principal...")
            menuPrincipal = new MenuPrincipal()

            splash.detener()
            iniciarSesion()
            menuPrincipal.iniciar()

            if(scannerActivo){
                scanMan = new DefaultScanMan()
                try {
                    println "comienza intento de conexi?n"
                    scanMan.connect(Principal.scannerPort, Principal.scannerBaudRate)
                    println "fin intento de conexi?n"
                } catch(Exception ex2) { Dialogos.error(ex2.getMessage(), ex2) }

                toFinalizeTracker.put("scanMan", "")

            }
           
            //
            //new SimpleForm() {
            //        it.form.visible = true
            //}
            /*
            def puerto = new PuertoNadesico()
            println puerto.dump()
            
            def objPrueba = puerto.ObjPrueba.newInstance()
            objPrueba.metodo1()
            objPrueba.metodo2("hola desde invocaci?n rara")
            println "resultado metodo 3: "+objPrueba.metodo3()
            println "resultado de la propiedad1 : " +objPrueba.prop1
            println "resultado de la propiedad2 : " +objPrueba.prop2
            println puerto.Articulos.get(145594).descripcion
            //puerto.(new Articulos(descripcion:"?ltimo objeto8")).addToCodigos(new CodigosArticulo(codigo:"lalalauiiuiui")).save()
            def art = (puerto.Articulos.newInstance(descripcion:"otro m?s")).addToCodigos(puerto.CodigosArticulo.newInstance(codigo:"?l c?digo")).save()
            //art.save()
            */
            } catch(e) {
                Dialogos.lanzarDialogoError(null, "Al iniciar aplicación: ${e.message}", Herramientas.getStackTraceString(e))
                logger.error("Al iniciar aplicación: ${e.message}".toString(), e)
                System.exit(1);
            }
            ///////////////////////

        }

    static def iniciarSesion() throws Exception {
        try{
        while(!SisUsuarios.login().cerrojo(SisUsuarios.CAJERO)) {}  // Aquí se detendrá el programa a esperar login
        escritorio.setNombreUsuario(SisUsuarios.usuarioActivo.nombre)
        } catch(e) { Dialogos.lanzarDialogoError(null, "Error al iniciar sesión en ciclo de huella: "+e.getMessage(), Herramientas.getStackTraceString(e)) }
    }

    static def cerrarSesion(){
        try{
                SisUsuarios.logout()
                escritorio.setNombreUsuario("Sin Sesión")
                Principal.menuPrincipal = new MenuPrincipal()
                Thread.start(){
                Principal.iniciarSesion()
                Principal.menuPrincipal.iniciar()
                }
        } catch(e) { Dialogos.lanzarDialogoError(null, "Error al cerrar secion ciclo de huella", Herramientas.getStackTraceString(e)) }
    }

    static def configExceptions() {
        if(SHOW_UNHANDLED_EXCEPTIONS) {
            Thread.setDefaultUncaughtExceptionHandler(new UEHandler());
        }
        //Logger.getRootLogger().addAppender(new CEAppender());
    }
}
