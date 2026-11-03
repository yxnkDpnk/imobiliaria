#!/usr/bin/env bash
set -euo pipefail

PROJETO_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
cd "$PROJETO_DIR"

# Procura somente a aplicação deste diretório, inclusive iniciada sem este script.
for processo in /proc/[0-9]*; do
    [[ "$(readlink "$processo/cwd" 2>/dev/null || true)" == "$PROJETO_DIR" ]] || continue
    argumentos=()
    mapfile -d '' -t argumentos < "$processo/cmdline" 2>/dev/null || continue
    [[ ${#argumentos[@]} -gt 0 ]] || continue
    [[ "${argumentos[0]##*/}" == java ]] || continue
    for argumento in "${argumentos[@]}"; do
        if [[ "$argumento" == com.example.primeiro_projeto.PrimeiroProjetoApplication ]]; then
            pid="${processo##*/}"
            echo "Parando a instância anterior (PID $pid)..."
            kill -TERM "$pid" 2>/dev/null || continue
            for ((tentativa = 0; tentativa < 30; tentativa++)); do
                kill -0 "$pid" 2>/dev/null || break
                sleep 0.5
            done
            if kill -0 "$pid" 2>/dev/null; then
                echo "A instância anterior ainda está encerrando. Tente novamente em alguns segundos." >&2
                exit 1
            fi
            break
        fi
    done
done

# Mantém a mesma chave entre reinicializações, sem colocá-la no código ou no Git.
umask 077
mkdir -p .local
chave_arquivo="$PROJETO_DIR/.local/jwt-secret"
if [[ ! -s "$chave_arquivo" ]]; then
    command -v openssl >/dev/null || { echo "Instale o OpenSSL para gerar a chave JWT." >&2; exit 1; }
    openssl rand -base64 32 > "$chave_arquivo"
fi
chmod 600 "$chave_arquivo"
JWT_SECRET="$(cat "$chave_arquivo")"
export JWT_SECRET

echo "Compilando e iniciando o Spring Boot. Para encerrar, pressione Ctrl+C."
exec ./mvnw clean compile spring-boot:run
