import { Box, Stack, Typography } from "@mui/material";

interface NuvolaLogoProps {
  size?: number;
  showTagline?: boolean;
  hideNameOnMobile?: boolean;
}

export function NuvolaLogo({
  size = 32,
  showTagline = false,
  hideNameOnMobile = false,
}: NuvolaLogoProps) {
  return (
    <Stack direction="row" spacing={1.5} sx={{ alignItems: "center" }}>
      <Box
        component="svg"
        viewBox="0 0 64 64"
        aria-hidden="true"
        sx={{ width: size, height: size, flexShrink: 0 }}
      >
        <g fill="currentColor">
          <circle cx="22" cy="36" r="10" />
          <circle cx="33" cy="28" r="13" />
          <circle cx="44" cy="37" r="9" />
          <rect x="22" y="36" width="22" height="10" />
        </g>
      </Box>
      <Box
        sx={
          hideNameOnMobile
            ? {
                display: "none",
                "@media (min-width: 375px)": { display: "block" },
              }
            : undefined
        }
      >
        <Typography
          component="span"
          sx={{
            display: "block",
            fontWeight: 700,
            fontSize: size * 0.65,
            lineHeight: 1.1,
          }}
        >
          Nuvola
        </Typography>
        {showTagline && (
          <Typography
            component="span"
            variant="body2"
            sx={{ display: "block", opacity: 0.85 }}
          >
            Portabilidade de convênios
          </Typography>
        )}
      </Box>
    </Stack>
  );
}