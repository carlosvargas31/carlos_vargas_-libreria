def call(Map config = [:]) {
    boolean abortPipeline = config.get('abortPipeline', false)
    
    try {
        echo "Iniciando análisis estático de código..."
        
        timeout(time: 5, unit: 'MINUTES') {
            withEnv(['SONAR_ENV=true']) {
                sh 'echo "Ejecución de las pruebas de calidad de código"'
            }
        }
        
        echo "Análisis estático completado exitosamente"
        
        echo "Evaluando QualityGate..."
        boolean qualityGatePassed = true
        
        if (!qualityGatePassed && abortPipeline) {
            error("QualityGate falló y abortPipeline está habilitado. Abortando pipeline...")
        } else if (!qualityGatePassed) {
            echo "Advertencia: QualityGate falló pero el pipeline continúa (abortPipeline = false)"
        }
        
    } catch (Exception e) {
        echo "Error en el análisis estático: ${e.message}"
        if (abortPipeline) {
            throw e
        }
    }
}