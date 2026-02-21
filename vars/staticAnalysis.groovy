def call(Map config = [:]) {
    // Parámetros con valores por defecto
    boolean abortPipeline = config.get('abortPipeline', false)
    
    try {
        echo "Iniciando análisis estático de código..."
        
        // Obtener el nombre de la rama actual
        String branchName = getBranchName()
        echo "Rama actual: ${branchName}"
        
        // Determinar si debe abortar basándose en la heurística
        boolean shouldAbort = determineShouldAbort(abortPipeline, branchName)
        echo "¿Abortar si falla QualityGate?: ${shouldAbort}"
        
        // Ejecución del análisis con timeout de 5 minutos
        timeout(time: 5, unit: 'MINUTES') {
            withEnv(['SONAR_ENV=true']) {
                sh '''
                    echo "Iniciando escaneo de código estático..."
                    echo "Analizando estructura del proyecto..."
                    sleep 2
                    echo "Verificando convenciones de nombres..."
                    sleep 2
                    echo "Analizando complejidad ciclomática..."
                    sleep 2
                    echo "Buscando vulnerabilidades de seguridad..."
                    sleep 2
                    echo "Ejecución de las pruebas de calidad de código"
                    sleep 2
                    echo "Generando reporte de análisis..."
                    sleep 2
                    echo "Análisis completado exitosamente"
                '''
            }
        }
        
        echo "Análisis estático completado exitosamente"
        
        // Evaluación del QualityGate
        echo "Evaluando QualityGate..."
        boolean qualityGatePassed = true // Simulación del resultado
        
        if (!qualityGatePassed && shouldAbort) {
            error("QualityGate falló en rama '${branchName}' y debe abortarse el pipeline.")
        } else if (!qualityGatePassed) {
            echo "Advertencia: QualityGate falló en rama '${branchName}' pero el pipeline continúa"
        } else {
            echo "QualityGate pasó correctamente"
        }
        
    } catch (Exception e) {
        echo "Error en el análisis estático: ${e.message}"
        throw e
    }
}

/**
 * Obtiene el nombre de la rama actual desde las variables de entorno de Jenkins
 */
def getBranchName() {
    try {
        // Intenta obtener desde BRANCH_NAME (multibranch pipelines)
        if (env.BRANCH_NAME) {
            return env.BRANCH_NAME
        }
        
        // Intenta obtener desde GIT_BRANCH y extrae el nombre sin "origin/"
        if (env.GIT_BRANCH) {
            String branch = env.GIT_BRANCH
            // Elimina el prefijo "origin/" si existe
            if (branch.contains('/')) {
                branch = branch.substring(branch.lastIndexOf('/') + 1)
            }
            return branch
        }
        
        // Si nada funciona, retorna unknown
        echo "Advertencia: No se pudo obtener el nombre de la rama desde variables de entorno"
        return "unknown"
    } catch (Exception e) {
        echo "Advertencia: Error al obtener el nombre de la rama: ${e.message}"
        return "unknown"
    }
}

/**
 * Determina si el pipeline debe abortarse según la heurística
 * 
 * Heurística:
 * - Si abortPipeline es true, siempre aborta
 * - Si la rama es "master", aborta
 * - Si la rama comienza con "hotfix", aborta
 * - En cualquier otro caso, no aborta
 */
def determineShouldAbort(boolean abortPipeline, String branchName) {
    // Regla 1: Si el parámetro abortPipeline es true, siempre abortar
    if (abortPipeline) {
        echo "Abortarán por parámetro abortPipeline = true"
        return true
    }
    
    // Regla 2: Si es rama master, abortar
    if (branchName == 'master') {
        echo "Abortarán porque la rama es 'master'"
        return true
    }
    
    // Regla 3: Si la rama comienza con "hotfix", abortar
    if (branchName.startsWith('hotfix')) {
        echo "Abortarán porque la rama comienza con 'hotfix'"
        return true
    }
    
    // Regla 4: En cualquier otro caso, no abortar
    echo "No abortará porque la rama es '${branchName}'"
    return false
}