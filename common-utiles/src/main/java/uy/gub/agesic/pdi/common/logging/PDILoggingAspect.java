package uy.gub.agesic.pdi.common.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.ThrowsAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
public class PDILoggingAspect implements ThrowsAdvice {

    @Around("@annotation(uy.gub.agesic.pdi.common.logging.Loggable)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = this.getLogger(joinPoint.getTarget().getClass());

        // Segun el nivel de debug, usamos un signature u otro
        String methodName = joinPoint.getSignature().getName();
        Long startTime = 0L;
        Long endTime = 0L;

        Object result = null;
        try {
            startTime = System.currentTimeMillis();

            // Ejecutamos el metodo, potencialmente con error:
            result = joinPoint.proceed();

        } catch (Throwable ex) {
            logger.error("(ERR) " + methodName, ex);
            throw ex;
        } finally {
            endTime = System.currentTimeMillis();

            if (logger.isDebugEnabled()) {
                long duration = endTime - startTime;
                MDC.put("duration", "" + duration);
                logger.debug("Operaci\u00F3n: " + methodName + " - " + duration + "(ms)");
                MDC.put("duration", "0");
            }

        }

        return result;
    }


    //@AfterThrowing(value = "@annotation(uy.gub.agesic.pdi.common.logging.Loggable)", throwing = "ex")
    public void AfterThrowing(JoinPoint joinPoint, Exception ex) throws Throwable {
        Logger logger = this.getLogger(joinPoint.getTarget().getClass());

        // Segun el nivel de debug, usamos un signature u otro
        String methodSignature = null;
        String methodName = joinPoint.getSignature().getName();
        String argumentValues = null;
        Long startTime = 0L;
        Long endTime = 0L;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        methodSignature = joinPoint.getSignature().toLongString();
        argumentValues = buildArgumentList(joinPoint.getArgs());

        logger.trace("[Clase y m\u00E9todo " + methodSignature + "] "
                + "[Argumentos " + argumentValues.toString() + "] "
                + "[Fecha y Hora " + dtf.format(now) + "] "
                + "[Host " + PDIHostName.HOST_NAME + "]"
        ) ;

        try {
            startTime = System.currentTimeMillis();

        } catch (Throwable e) {
            logger.error("(ERR) " + methodName, e);
            throw e;
        } finally {
            endTime = System.currentTimeMillis();

            if (logger.isTraceEnabled()) {
                logger.trace("(FIN) " + methodName + " Tiempo ejecuci\u00F3n (milis): " + (endTime - startTime));
            } else if (logger.isDebugEnabled()) {
                logger.trace("(FIN) " + methodName + " Tiempo ejecuci\u00F3n (milis): " + (endTime - startTime));
            } else {
                logger.trace("(FIN) " + methodName + " Tiempo ejecuci\u00F3n (milis): " + (endTime - startTime));
            }
        }

    }



    private String buildArgumentList(Object[] args) {
        StringBuilder sb = new StringBuilder("");

        if (args != null && args.length > 0) {
            for (Object arg : args) {
                sb.append(String.valueOf(arg));
                sb.append(" ,");
            }
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }

    private Logger getLogger(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        return logger;
    }

}
