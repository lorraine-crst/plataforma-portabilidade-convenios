import { useCallback, useEffect, useState, type DependencyList } from "react";
import { ApiError } from "../api/ApiError";

interface ResultadoBusca<T> {
  dados: T | null;
  carregando: boolean;
  erro: ApiError | null;
  recarregar: () => void;
}

export function useBuscar<T>(
  funcao: () => Promise<T>,
  dependencias: DependencyList,
): ResultadoBusca<T> {
  const [dados, setDados] = useState<T | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<ApiError | null>(null);
  const [versao, setVersao] = useState(0);

  useEffect(() => {
    let ativo = true;

    setCarregando(true);
    setErro(null);

    funcao()
      .then((resultado) => {
        if (ativo) {
          setDados(resultado);
        }
      })
      .catch((causa: unknown) => {
        if (ativo) {
          setErro(
            causa instanceof ApiError
              ? causa
              : new ApiError("SEM_CONEXAO", "Ocorreu um erro inesperado.", 0),
          );
        }
      })
      .finally(() => {
        if (ativo) {
          setCarregando(false);
        }
      });

    return () => {
      ativo = false;
    };
  }, [...dependencias, versao]);

  const recarregar = useCallback(() => {
    setVersao((atual) => atual + 1);
  }, []);

  return { dados, carregando, erro, recarregar };
}
