def call(Map config = [:]) {
    boolean abortPipeline = config.get('abortPipeline', false)
    
    try {
        echo "Iniciando análisis estático de código..."
        
        String branchName = getBranchName()
        echo "Rama actual: ${branchName}"
        
        boolean shouldAbort = determineShouldAbort(abortPipeline, branchName)
        echo "¿Abortar si falla QualityGate?: ${shouldAbort}"
        
        // Ejecución del análisis con timeout de 5 minutos
        timeout(time: 5, unit: 'MINUTES') {
            // Usar SonarQube Scanner
            withSonarQubeEnv('SonarQube') {
                sh '''
                    echo "Iniciando escaneo de código estático con SonarQube..."
                    echo "Analizando estructura del proyecto..."
                    sleep 2
                    echo "Verificando convenciones de nombres..."
                    sleep 2
                    echo "Analizando complejidad ciclomática..."
                    sleep 2
                    echo "Buscando vulnerabilidades de seguridad..."
                    sleep 2
                    
                    # Ejecutar SonarQube Scanner
                    sonar-scanner \
                        -Dsonar.projectKey=carlos-vargas-libreria \
                        -Dsonar.sources=. \
                        -Dsonar.host.url=$SONAR_HOST_URL \
                        -Dsonar.login=$SONAR_AUTH_TOKEN
                    
                    echo "Ejecución de las pruebas de calidad de código"
                    sleep 2
                    echo "Generando reporte de análisis..."
                    sleep 2
                    echo "Análisis completado exitosamente"
                '''
            }
        }
        
        echo "Análisis estático completado exitosamente"
        
        // Esperar y evaluar el QualityGate
        echo "Evaluando QualityGate..."
        timeout(time: 5, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: shouldAbort
        }
        
        echo "QualityGate evaluado correctamente"
        
    } catch (Exception e) {
        echo "Error en el análisis estático: ${e.message}"
        throw e
    }
}

def getBranchName() {
    if (env.BRANCH_NAME) {
        return env.BRANCH_NAME
    }
    
    if (env.GIT_BRANCH) {
        String branch = env.GIT_BRANCH
        if (branch.contains('/')) {
            branch = branch.substring(branch.lastIndexOf('/') + 1)
        }
        return branch
    }
    
    echo "Advertencia: No se pudo obtener el nombre de la rama"
    return "unknown"
}

def determineShouldAbort(boolean abortPipeline, String branchName) {
    if (abortPipeline) {
        echo "Abortarán por parámetro abortPipeline = true"
        return true
    }
    
    if (branchName == 'master') {
        echo "Abortarán porque la rama es 'master'"
        return true
    }
    
    if (branchName.startsWith('hotfix')) {
        echo "Abortarán porque la rama comienza con 'hotfix'"
        return true
    }
    
    echo "No abortará porque la rama es '${branchName}'"
    return false
}