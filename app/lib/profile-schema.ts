import { z } from "zod";

export const profileSchema = z.object({
  rut: z.string().regex(/^[0-9]{7,8}[0-9K]$/, "RUT sin puntos ni guion, ej: 123456785"),
  birthDate: z
    .string()
    .min(1, "Ingresa tu fecha de nacimiento")
    .refine((value) => new Date(value) < new Date(), "La fecha de nacimiento debe ser pasada"),
  cardNumber: z.string().regex(/^[0-9]{13,19}$/, "El número de tarjeta debe tener entre 13 y 19 dígitos"),
  cardHolderName: z.string().trim().min(1, "Ingresa el nombre del titular").max(100, "Máximo 100 caracteres"),
  cardExpiry: z.string().regex(/^(0[1-9]|1[0-2])\/[0-9]{2}$/, "Vencimiento en formato MM/AA"),
});

export type ProfileData = z.infer<typeof profileSchema>;
