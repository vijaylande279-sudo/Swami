export interface PlanSummary {
  planId: string;
  planKey: string;
  billingInterval: string;
  pricePaise: number;
  gstPaise: number;
  totalPaise: number;
}

export interface AppSummary {
  key: string;
  name: string;
  description: string | null;
  plan: PlanSummary | null;
}
