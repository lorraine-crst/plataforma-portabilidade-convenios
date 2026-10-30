import { useCallback, useEffect, useState, type DependencyList } from "react";
import { ApiError } from "../api/ApiError";

interface FetchResult<T> {
  data: T | null;
  isLoading: boolean;
  error: ApiError | null;
  reload: () => void;
}

export function useFetch<T>(
  fetcher: () => Promise<T>,
  dependencies: DependencyList,
): FetchResult<T> {
  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let isActive = true;

    setIsLoading(true);
    setError(null);

    fetcher()
      .then((result) => {
        if (isActive) {
          setData(result);
        }
      })
      .catch((cause: unknown) => {
        if (isActive) {
          setError(
            cause instanceof ApiError
              ? cause
              : new ApiError("SEM_CONEXAO", "Ocorreu um erro inesperado.", 0),
          );
        }
      })
      .finally(() => {
        if (isActive) {
          setIsLoading(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [...dependencies, version]);

  const reload = useCallback(() => {
    setVersion((current) => current + 1);
  }, []);

  return { data, isLoading, error, reload };
}
